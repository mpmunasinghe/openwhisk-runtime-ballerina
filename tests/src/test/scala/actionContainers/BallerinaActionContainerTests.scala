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

import actionContainers.ActionContainer.withContainer
import common.WskActorSystem
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.json._

@RunWith(classOf[JUnitRunner])
class BallerinaActionContainerTests extends ActionProxyContainerTestUtils with WskActorSystem {

  lazy val ballerinaContainerImageName = "mpmunasinghe/balaction"

  def withBallerinaContainer(code: ActionContainer => Unit, env: Map[String, String] = Map.empty) =
    withContainer(ballerinaContainerImageName, env)(code)

  behavior of ballerinaContainerImageName

  it should "Initialize with the hello code" in {
    val (out, err) = withBallerinaContainer { c =>
      val path = getClass.getResource("/hello-function.balx").getPath
      val encoded = Base64.getEncoder.encode(Files.readAllBytes(Paths.get(path)))
      val sourceString = new String(encoded, "UTF-8")

      val (initCode, _) = c.init(initPayload(sourceString))
      initCode should be(200)

      val (runCode, runRes) = c.run(runPayload(JsObject()))
      runRes should be(Some(JsObject("response" -> JsString("hello-world"))))
    }
  }
}
