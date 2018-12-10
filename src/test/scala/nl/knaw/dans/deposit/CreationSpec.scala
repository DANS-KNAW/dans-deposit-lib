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
import org.joda.time.DateTime

import scala.util.{ Failure, Success }

class CreationSpec extends TestSupportFixture {

  "load" should "yield a Creation object with a timestamp" in {
    val dateTime = DateTime.parse("2018-05-25T20:08:56.210+02:00", dateTimeFormatter)
    val props = new PropertiesConfiguration()
    props.addProperty("creation.timestamp", dateTime.toString(dateTimeFormatter))

    Creation.load(props) should matchPattern { case Success(Creation(`dateTime`)) => }
  }

  it should "fail when timestamp is not present" in {
    val props = new PropertiesConfiguration()

    Creation.load(props) should matchPattern { case Failure(IllegalArgumentException("could not find mandatory field 'creation.timestamp'", _)) => }
  }

  "timestampString" should "return a string representation that is equal to original input" in {
    val dateTime = DateTime.parse("2018-05-25T20:08:56.210+02:00", dateTimeFormatter)

    Creation(dateTime).timestampString shouldBe "2018-05-25T20:08:56.210+02:00"
  }
}
