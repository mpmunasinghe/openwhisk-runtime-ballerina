<!--
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
-->

# Apache OpenWhisk Runtime for Ballerina

[![Build Status](https://travis-ci.com/mpmunasinghe/openwhisk-runtime-ballerina.svg?branch=master)](https://travis-ci.com/mpmunasinghe/openwhisk-runtime-ballerina)

This repository contains the [Ballerina](https://ballerinalang.org) extension for Apache OpenWhisk serverless platform.

## Prerequisites

The following prerequisites are needed to try this out:

- [Ballerina](https://ballerina.io/downloads/) >= 0.975.0

## Quick Ballerina Action

 Create a Ballerina function file with the following content and name it as hello-function.bal:

   ```ballerina
    import ballerina/io;
    function main(string... args) {
       io:println("started");
    }
    function run(json jsonInput) returns json {
       io:println(jsonInput);
       json output = { "response": "hello-world"};
       return output;
    }
   ```
Note that the ballerina file should include both **main(string... args)** function and **run(json jsonInput)**. main(string... args) function is used to compile the ballerina function

Run ```ballerina build hello-function.bal``` to build the above function. [Ballerina](https://ballerina.io/downloads/)

### Create the Ballerina Action
Create an OpenWhisk action for the above Ballerina function using the OpenWhisk CLI:

To use as a docker action:

```bash
wsk action update hello-function hello-function.balx --docker mpmunasinghe/balaction
```

To use on a deployment of OpenWhisk that contains the runtime as a kind:

```bash
wsk action create hello-function hello-function.balx
```

### Invoke the Ballerina Action
Invoke the hello-function using the OpenWhisk CLI:

```bash
wsk action invoke hello-function --result
{
    "response": "hello-world"
}
```

### Testing
Install dependencies from the root directory on $OPENWHISK_HOME repository
```bash
pushd $OPENWHISK_HOME
./gradlew install
podd $OPENWHISK_HOME
```

Using gradle to run all tests
```bash
./gradlew :tests:test
```
Using gradle to run some tests
```bash
./gradlew :tests:test --tests *ActionContainerTests*
```
Using IntelliJ:
- Import project as gradle project.
- Make sure working directory is root of the project/repo

# License
[Apache 2.0](LICENSE.txt)
