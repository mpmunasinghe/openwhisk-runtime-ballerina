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
import org.apache.commons.io.IOUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.wso2.msf4j.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public class BalxLoader {

    public static Path saveBase64EncodedFile(InputStream encoded) throws IOException {
        Base64.Decoder decoder = Base64.getDecoder();

        InputStream decoded = decoder.wrap(encoded);

        File destinationFile = File.createTempFile(Constants.FUNCTION_FILE_NAME, Constants.FUNCTION_EXTENSION);
        destinationFile.deleteOnExit();
        Path destinationPath = destinationFile.toPath();

        Files.copy(decoded, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return destinationPath;
    }

    public static ProgramFile initProgramFile (ProgramFile programFile){
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);

        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }

        programFile.initializeGlobalMemArea();
        return programFile;
    }

    public static JsonObject requestToJson(Request request) throws IOException {
        JsonParser parser = new JsonParser();
        //List<ByteBuffer> byteBuffers = request.getFullMessageBody();
        InputStream inputStream = request.getMessageContentStream();
        String inputString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//        StringBuilder req = new StringBuilder();
//        for (ByteBuffer buffer : byteBuffers) {
//            req.append(Charset.forName(StandardCharsets.UTF_8.name()).decode(buffer).toString());
//        }
//        return parser.parse(req.toString()).getAsJsonObject();
        return parser.parse(inputString).getAsJsonObject();
    }

}
