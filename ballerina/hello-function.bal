import ballerina/io;

function main(string... args) {
   io:println("started");
}

function ballerinaMain(json jsonInput) returns json {
   io:println(jsonInput);
   json output = { "response": "hellow-world"};
   return output;
}
