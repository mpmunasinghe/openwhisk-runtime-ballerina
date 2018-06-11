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

package actionContainers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ActionContainer.withContainer

import common.WskActorSystem

import spray.json._

@RunWith(classOf[JUnitRunner])
class BallerinaActionContainerTests extends ActionProxyContainerTestUtils with WskActorSystem {

  lazy val ballerinaContainerImageName = "us.gcr.io/inner-deck-199908/ballerina-runtime"

  def withBallerinaContainer(code: ActionContainer => Unit, env: Map[String, String] = Map.empty) =
    withContainer(ballerinaContainerImageName, env)(code)

  behavior of ballerinaContainerImageName

    it should "Initialize with the hello code" in {
      val (out, err) = withBallerinaContainer { c =>
        val code = """
                     | import ballerina/io;
                     |
                     | function main(string... args) {
                     |    json output = { "hello": "from Ballerina!" };
                     |    io:println(output);
                     | }
                   """.stripMargin

        val (initCode, _) = c.init(initPayload(code))
        initCode should be (200)
      }
    }

  it should "Initialize and run the hello code" in {
    val (out, err) = withBallerinaContainer { c =>
      val code = """
                   | import ballerina/io;
                   |
                   | function main(string... args) {
                   |    json output = { "hello": "from Ballerina!" };
                   |    io:println(output);
                   | }
                 """.stripMargin

      val (initCode, _) = c.init(initPayload(code))
      initCode should be (200)

      val (runCode, runRes) = c.run(runPayload(JsObject()))
      println(runRes)
      runCode should be (200)
    }
  }
}
