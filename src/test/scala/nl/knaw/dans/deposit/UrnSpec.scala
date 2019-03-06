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

class UrnSpec extends TestSupportFixture {
  "load" should "yield a Urn object with value present" in {
    val urn = "urn:dans:easy:some:test:xml"
    val props = new PropertiesConfiguration()
    props.addProperty("identifier.urn", urn)

    Urn.load(props) shouldBe Urn(Some(urn))
  }

  it should "yield a urn object with no value present" in {
    val props = new PropertiesConfiguration()

    Urn.load(props) shouldBe Urn(None)
  }
}
