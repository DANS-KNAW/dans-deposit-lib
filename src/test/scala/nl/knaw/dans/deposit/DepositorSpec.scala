/**
 * Copyright (C) 2018 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.deposit

import nl.knaw.dans.deposit.fixtures.TestSupportFixture
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.{ Failure, Success }

class DepositorSpec extends TestSupportFixture {

  "load" should "yield a Depositor object with userId present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("depositor.userId", "myuser")

    Depositor.load(props) should matchPattern { case Success(Depositor("myuser")) => }
  }

  it should "fail when userId is not present" in {
    val props = new PropertiesConfiguration()
    // no depositor.userId

    Depositor.load(props) should matchPattern { case Failure(IllegalArgumentException("could not find mandatory field 'depositor.userId'", _)) => }
  }
}
