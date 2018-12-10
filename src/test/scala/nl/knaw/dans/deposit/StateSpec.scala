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

class StateSpec extends TestSupportFixture {

  "load" should "yield a State object with label and description present" in {
    val description = "any random text"
    val props = new PropertiesConfiguration()
    props.addProperty("state.label", "FINALIZING")
    props.addProperty("state.description", description)

    State.load(props) should matchPattern { case Success(State(StateLabel.FINALIZING, `description`)) => }
  }

  it should "fail when no label is provided" in {
    val description = "any random text"
    val props = new PropertiesConfiguration()
    // no state.label
    props.addProperty("state.description", description)

    State.load(props) should matchPattern {
      case Failure(IllegalArgumentException("could not find mandatory field 'state.label'", _)) =>
    }
  }

  it should "fail when an invalid label is provided" in {
    val description = "any random text"
    val props = new PropertiesConfiguration()
    props.addProperty("state.label", "invalid value")
    props.addProperty("state.description", description)

    State.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'state.label'", _)) =>
    }
  }

  it should "fail when no description is provided" in {
    val props = new PropertiesConfiguration()
    props.addProperty("state.label", "FINALIZING")
    // no state.description

    State.load(props) should matchPattern {
      case Failure(IllegalArgumentException("could not find mandatory field 'state.description'", _)) =>
    }
  }
}
