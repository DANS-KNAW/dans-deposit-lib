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

class IngestSpec extends TestSupportFixture {

  "load" should "yield a Ingest object with currentStep present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("deposit.ingest.current-step", "SPRINGFIELD")

    Ingest.load(props) should matchPattern { case Success(Ingest(Some(CurrentIngestStep.SPRINGFIELD))) => }
  }

  it should "fail when an invalid value is given for currentStep" in {
    val props = new PropertiesConfiguration()
    props.addProperty("deposit.ingest.current-step", "invalid value")

    Ingest.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'deposit.ingest.current-step'", _)) =>
    }
  }
}
