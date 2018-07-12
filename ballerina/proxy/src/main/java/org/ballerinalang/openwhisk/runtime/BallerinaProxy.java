/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.openwhisk.runtime;

import com.google.gson.JsonObject;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.msf4j.Request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * OpenWhisk Ballerina Runtime Proxy Service
 * Exposes /init and /run resources
 */
@Path("/") public class BallerinaProxy {
    private ProgramFile programFile;

    @POST
    @Path("init")
    public Response init(@Context Request request) throws IOException {
        InputStream ballerinaIs = null;
        try {
            JsonObject requestElements = BalxLoader.requestToJson(request).getAsJsonObject(Constants.JSON_VALUE);
            Boolean isBinary = requestElements.get(Constants.BINARY).getAsBoolean();

            String ballerinaCode = requestElements.get(Constants.CODE).getAsString();

            ballerinaIs = new ByteArrayInputStream(ballerinaCode.getBytes(StandardCharsets.UTF_8));

            // Check for binary value. .balx should be received with the binary parameter
            if (isBinary) {
                java.nio.file.Path destinationPath = BalxLoader.saveBase64EncodedFile(ballerinaIs);

                programFile = BLangProgramLoader.read(destinationPath);

                return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                               .entity("{ 'success' : 'Function init success'}").build();
            } else {
                java.nio.file.Path destinationPath = BalxLoader.saveBalFile(ballerinaIs);

                String tempDir = new File(destinationPath.toString()).getAbsoluteFile().getParent();

                if (BalxLoader.buildBal(tempDir)) {
                    java.nio.file.Path balxString = java.nio.file.Paths.get(destinationPath + "x");
                    programFile = BLangProgramLoader.read(balxString);

                    return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                                   .entity("{ 'success' : 'Function init success'}").build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                                   .entity("{ 'error' : 'Bad content request'}").build();
                }
            }
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Internal server error'}").build();
        } finally {
            if (ballerinaIs != null) {
                ballerinaIs.close();
            }
        }
    }

    @POST
    @Path("run")
    public Response run(@Context Request request) {
        Optional<ProgramFile> optionalValue = Optional.ofNullable(programFile);
        JsonObject requestElements;
        BValue[] result;

        // Check whether init function has success and the program file is set
        if (!optionalValue.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Function not initialized'}").build();
        }

        programFile = BalxLoader.initProgramFile(programFile);

        requestElements = BalxLoader.requestToJson(request);

        BValue bJson = new BJSON(requestElements.getAsJsonObject(Constants.JSON_VALUE).toString());
        BValue[] parameters = new BValue[1];
        parameters[0] = bJson;

        try {
            result = BLangFunctions.invokeEntrypointCallable(programFile, programFile.getEntryPkgName(),
                                                             Constants.FUNCTION_CALLABLE_NAME, parameters);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Running Function failed'}").build();
        }

        StringBuilder response = new StringBuilder();
        for (BValue bValue : result) {
            response.append(bValue.stringValue());
        }
        return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                       .entity(response.toString()).build();
    }

}
