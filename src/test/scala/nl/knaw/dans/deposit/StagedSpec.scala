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

class StagedSpec extends TestSupportFixture {

  "load" should "yield a Staged object with state present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("staged.state", "REJECTED")

    Staged.load(props) should matchPattern { case Success(Staged(Some(StageState.REJECTED))) => }
  }

  it should "yield a Staged object with no state present" in {
    val props = new PropertiesConfiguration()
    // no staged.state

    Staged.load(props) should matchPattern { case Success(Staged(None)) => }
  }

  it should "fail when playMode has an invalid value" in {
    val props = new PropertiesConfiguration()
    props.addProperty("staged.state", "invalid value")

    Staged.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'staged.state'", _)) =>
    }
  }
}
