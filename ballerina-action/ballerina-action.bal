import ballerina/http;
import ballerinax/docker;
import ballerina/io;
import ballerina/internal;

@docker:Config {
    registry:"mpmunasinghe",
    name:"ballerina-runtime",
    tag:"latest"
}

@docker:Expose{}
endpoint http:Listener listener {
    port:8080
};

@http:ServiceConfig {
    basePath: "/"
}
service<http:Service> greeting bind listener {

    @http:ResourceConfig {
        path: "init"
    }
    init(endpoint caller, http:Request request) {
        http:Response response = new;
        string fileName;
        json reqPayload = check request.getJsonPayload();

        boolean isBinaryFile = check <boolean>reqPayload.value.binary;
        blob blobContent = reqPayload.value.code.toString().toBlob("UTF-8");

        if (isBinaryFile) {
            fileName = "function.balx";
            io:ByteChannel fileChannel = io:openFile(fileName, io:WRITE);

            blobContent = blobContent.base64Decode();
            int i = check fileChannel.write(blobContent, 0);

            response.statusCode = http:ACCEPTED_202;
        } else {
            fileName = "function.bal";
            io:ByteChannel fileChannel = io:openFile(fileName, io:WRITE);

            int i = check fileChannel.write(blobContent, 0);

            internal:BallerinaCommand build = "build";
            var buildResult = filterResult(check internal:execBallerina(build, fileName));

            match buildResult {
                string outString => {
                    response.statusCode = http:ACCEPTED_202;
                }
                error err => {
                    response.statusCode = 500;
                    response.setJsonPayload({error : "Error occurred while building the function " + err.message});
                }
            }
        }
        _ = caller->respond(response);
    }

    @http:ResourceConfig {
        path: "run"
    }
    run(endpoint caller, http:Request request) {
        http:Response response = new;
        internal:BallerinaCommand run = "run";
        var execResult = filterResult(check internal:execBallerina(run, "function.balx"));

        match execResult {
            string outString => {
                string[] stringArr = outString.split("\n");
                int lastIndex = lengthof stringArr - 1;
                response.setJsonPayload(stringArr[lastIndex]);
            }
            error err => {
                response.setJsonPayload({error : "Error occurred while running the function " + err.message});
            }
        }
        _ = caller->respond(response);
    }
}

function filterResult(string result) returns (string|error) {
    if (result.contains("error:")) {
        error err = { message : result };
        return err;
    } else {
        return result;
    }
}