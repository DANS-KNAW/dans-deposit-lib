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

class Sword2Spec extends TestSupportFixture {

  "load" should "yield a Sword2 object with contentType present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("easy-sword2.client-message.content-type", "application/zip")

    Sword2.load(props) should matchPattern { case Success(Sword2(Some(Sword2ContentType.ZIP))) => }
  }

  it should "yield a Sword2 object with no contentType present" in {
    val props = new PropertiesConfiguration()
    // no easy-sword2.client-message.content-type

    Sword2.load(props) should matchPattern { case Success(Sword2(None)) => }
  }

  it should "fail when contentType has an invalid value" in {
    val props = new PropertiesConfiguration()
    props.addProperty("easy-sword2.client-message.content-type", "invalid value")

    Sword2.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'easy-sword2.client-message.content-type'", _)) =>
    }
  }
}
