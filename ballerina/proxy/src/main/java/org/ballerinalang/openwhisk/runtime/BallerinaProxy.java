/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.openwhisk.runtime;

import com.google.gson.JsonObject;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.msf4j.Request;

import java.io.ByteArrayInputStream;
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
 * OpenWhisk Ballerina Proxy runtime Service
 */
@Path("/") public class BallerinaProxy {
    private ProgramFile programFile;

    @POST
    @Path("init")
    public Response init(@Context Request request) throws IOException {
        JsonObject requestElements = BalxLoader.requestToJson(request).getAsJsonObject(Constants.JSON_VALUE);
        Boolean isBinary = requestElements.get(Constants.BINARY).getAsBoolean();
        if (isBinary) {
            String base64Balx = requestElements.get(Constants.CODE).getAsString();
            InputStream balxIs = new ByteArrayInputStream(base64Balx.getBytes(StandardCharsets.UTF_8));
            try {
                java.nio.file.Path destinationPath = BalxLoader.saveBase64EncodedFile(balxIs);

                programFile = BLangProgramLoader.read(destinationPath);

                return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                               .entity("{ 'success' : 'function init success'}").build();
            } catch (IOException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                               .entity("{ 'error' : 'Internal Server Error'}").build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Bad content request'}").build();
        }
    }

    @POST
    @Path("run")
    public Response run(@Context Request request) throws IOException {
        Optional<ProgramFile> optionalValue = Optional.ofNullable(programFile);

        if (!optionalValue.isPresent()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Internal Server Error'}").build();
        }

        JsonObject requestElements = BalxLoader.requestToJson(request);
        BValue bjson = new BJSON(requestElements.getAsJsonObject(Constants.JSON_VALUE).toString());
        BValue[] parameters = new BValue[1];
        parameters[0] = bjson;

        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);

        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }

        programFile.initializeGlobalMemArea();

        BValue[] result = BLangFunctions
                .invokeEntrypointCallable(programFile, programFile.getEntryPkgName(), Constants.FUNCTION_CALLABLE_NAME,
                                          parameters);

        StringBuilder response = new StringBuilder();
        for (BValue bValue : result) {
            response.append(bValue.stringValue());
        }
        return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                       .entity(response.toString()).build();

    }

}
