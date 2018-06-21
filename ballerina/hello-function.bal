import ballerina/io;

function main(string... args) {
   io:println("started");
   json hello = ballerinaMain("{'names': ['J.J.', 'April'], 'years': [25, 29]}");
   io:println(hello);
}

function ballerinaMain(json jsonInput) returns json {
   io:println(jsonInput);
   json output = { "response": "res"};
   string output2 = "response";
   return output;
}
