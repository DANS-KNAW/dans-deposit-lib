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

import scala.util.Success

class IdentifierSpec extends TestSupportFixture {

  "load" should "yield a Identifier object all fields present" in {
    val doi = "some-random-doi"
    val fedoraId = "some fedora id"
    val urn = "urn:dans:easy:some:test:xml"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.doi", doi)
    props.addProperty("identifier.dans-doi.action", "update")
    props.addProperty("identifier.dans-doi.registered", "yes")
    props.addProperty("identifier.fedora", "some fedora id")
    props.addProperty("identifier.fedora", fedoraId)
    props.addProperty("identifier.urn", urn)

    Identifier.load(props) should matchPattern {
      case Success(Identifier(Doi(Some(`doi`), Some(true), Some(Action.update)), FedoraId(Some(`fedoraId`)), Urn(Some(`urn`)))) =>
    }
  }
}
