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

class DoiSpec extends TestSupportFixture {

  "load" should "yield a Doi object with doi, action and registered present" in {
    val doi = "some-random-doi"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    props.addProperty("identifier.dans-doi.action", "update")
    props.addProperty("identifier.dans-doi.registered", "yes")

    Doi.load(props) should matchPattern { case Success(Doi(Some(`doi`), Some(true), Some(Action.update))) => }
  }

  it should "yield a Doi object with no doi present" in {
    val props = new PropertiesConfiguration()
    // no identifier.doi
    props.addProperty("identifier.dans-doi.action", "update")
    props.addProperty("identifier.dans-doi.registered", "yes")

    Doi.load(props) should matchPattern { case Success(Doi(None, Some(true), Some(Action.update))) => }
  }

  it should "yield a Doi object with no action present" in {
    val doi = "some-random-doi"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    // no identifier.dans-doi.action
    props.addProperty("identifier.dans-doi.registered", "yes")

    Doi.load(props) should matchPattern { case Success(Doi(Some(`doi`), Some(true), None)) => }
  }

  it should "yield a Doi object with no registered present" in {
    val doi = "some-random-doi"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    props.addProperty("identifier.dans-doi.action", "update")
    // no identifier.dans-doi.registered

    Doi.load(props) should matchPattern { case Success(Doi(Some(`doi`), None, Some(Action.update))) => }
  }

  it should "fail when an invalid value was given for action" in {
    val doi = "some-random-doi"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    props.addProperty("identifier.dans-doi.action", "invalid value")
    props.addProperty("identifier.dans-doi.registered", "yes")

    Doi.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'identifier.dans-doi.action'", _)) =>
    }
  }

  it should "yield a Doi object with archived = false when this property has an invalid value" in {
    val doi = "some-random-doi"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    props.addProperty("identifier.dans-doi.action", "update")
    props.addProperty("identifier.dans-doi.registered", "invalid value")

    Doi.load(props) should matchPattern { case Success(Doi(Some(`doi`), Some(false), Some(Action.update))) => }
  }

  "registeredString" should "convert the registered field true back to a string" in {
    Doi(Option("doi"), Option(true), Option(Action.create)).registeredString.value shouldBe "yes"
  }

  it should "convert the registered field false back to a string" in {
    Doi(Option("doi"), Option(false), Option(Action.create)).registeredString.value shouldBe "no"
  }

  it should "not convert an unpresent registered field" in {
    Doi(Option("doi"), None, Option(Action.create)).registeredString shouldBe empty
  }
}
