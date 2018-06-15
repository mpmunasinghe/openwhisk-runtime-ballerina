import ballerina/io;

function main(string... args) {
   io:println("started");
}

function ballerinaMain(json jsonInput) returns json {
   json output = { "response": jsonInput};
   return output;
}
