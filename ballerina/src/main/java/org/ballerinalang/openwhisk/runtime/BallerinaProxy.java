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
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.msf4j.Request;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
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
    public Response init(@Context Request request) {
        JsonObject requestElements = requestToJson(request).getAsJsonObject(Constants.JSON_VALUE);
        Boolean isBinary = requestElements.get(Constants.BINARY).getAsBoolean();
        if (isBinary) {
            String code = requestElements.get(Constants.CODE).getAsString();
            byte[] decodedProgramFile = Base64.getDecoder().decode(code);
            try {
                FileUtils.writeByteArrayToFile(new File(Constants.FUNCTION_FILE_NAME), decodedProgramFile);
            } catch (IOException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                               .entity("{ 'error' : 'Internal Server Error'}").build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Bad content request'}").build();
        }
        java.nio.file.Path path = Paths.get(Constants.FUNCTION_FILE_NAME);
        programFile = BLangProgramLoader.read(path);
        return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                       .entity("{ 'success' : 'function init success'}").build();
    }

    @POST
    @Path("run")
    public Response run(@Context Request request) {
        Optional<ProgramFile> optionalValue = Optional.ofNullable(programFile);
        if (optionalValue.isPresent()) {
            JsonObject requestElements = requestToJson(request);
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
            BValue[] result = BLangFunctions.invokeEntrypointCallable(programFile, programFile.getEntryPkgName(),
                                                                      Constants.FUNCTION_CALLABLE_NAME, parameters);

            StringBuilder response = new StringBuilder();
            for (BValue bValue : result) {
                response.append(bValue.stringValue());
            }
            return Response.status(Response.Status.OK).header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity(response.toString()).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .header(HttpHeaders.CONTENT_ENCODING, Constants.IDENTITY)
                           .entity("{ 'error' : 'Internal Server Error'}").build();
        }
    }

    private JsonObject requestToJson(Request request) {
        JsonParser parser = new JsonParser();
        List<ByteBuffer> byteBuffers = request.getFullMessageBody();
        StringBuilder req = new StringBuilder();
        for (ByteBuffer buffer : byteBuffers) {
            req.append(Charset.forName(StandardCharsets.UTF_8.name()).decode(buffer).toString());
        }
        return parser.parse(req.toString()).getAsJsonObject();
    }
}
