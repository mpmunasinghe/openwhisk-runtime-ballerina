# Apache OpenWhisk Runtime for Ballerina

This repository contains the [Ballerina](https://ballerinalang.org) hellonative extension for Apache OpenWhisk serverless platform.

## Prerequisites

The following prerequisites are needed to try this out:

- [Vagrant](https://www.vagrantup.com/downloads.html) >= v2.0.1
- [OpenWhisk](https://github.com/apache/incubator-openwhisk.git)
- [OpenWhisk CLI](https://github.com/apache/incubator-openwhisk-cli)
- [Ballerina](https://ballerina.io/downloads/) >= 0.974.1-SNAPSHOT (Requires to build the function)

## Quick Start Guide

1. Install OpenWhisk using Vagrant:

   ```bash
   # Clone OpenWhisk git repository
   git clone --depth=1 https://github.com/apache/incubator-openwhisk.git openwhisk

   # Switch the directory to tools/vagrant
   cd openwhisk/tools/vagrant

   # Start OpenWhisk instance
   vagrant up
   ```

2. Install OpenWhisk CLI by following it's installation guide:
   https://github.com/apache/incubator-openwhisk-cli

3. Create a Ballerina function file with the following content and name it as hello-function.bal:

   ```
    import ballerina/io;
    
    function main(string... args) {
       io:println("started");
    }
    
    function ballerinaMain(json jsonInput) returns json {
       json output = { "response": jsonInput};
       return output;
    }
   ```
   
   Note that the ballerina file should include both **main(string... args)** function and **ballerinaMain(json 
   jsonInput)**. main(string... args) function is used to compile the ballerina function
   
4. Run ballerina build hello-function.bal to build the above function.    

5. Create an OpenWhisk action for the above Ballerina function using the OpenWhisk CLI:
   
   ```bash
   wsk action create hello-function hello-function.balx
   ```

6. Invoke the hello-function using the OpenWhisk CLI:

   ```bash
   wsk action invoke hello-function --result -p myresult "hello"
   {
       "response": {
           "myresult": "hello"
       }
   }
   ```
