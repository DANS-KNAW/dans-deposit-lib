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

class CurationSpec extends TestSupportFixture {

  "load" should "yield a Curation object with all fields present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")
    props.addProperty("curation.is-new-version", "yes")
    props.addProperty("curation.required", "no")
    props.addProperty("curation.performed", "yes")

    Curation.load(props) shouldBe Curation(DataManager(Some("myadmin"), Some("FILL.IN.YOUR@VALID-EMAIL.NL")), Some(true), Some(false), Some(true))
  }

  it should "yield a Curation object without isNewVersion property" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")
    // no curation.is-new-version
    props.addProperty("curation.required", "no")
    props.addProperty("curation.performed", "yes")

    Curation.load(props) shouldBe Curation(DataManager(Some("myadmin"), Some("FILL.IN.YOUR@VALID-EMAIL.NL")), None, Some(false), Some(true))
  }

  it should "yield a Curation object without required property" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")
    props.addProperty("curation.is-new-version", "yes")
    // no curation.required
    props.addProperty("curation.performed", "yes")

    Curation.load(props) shouldBe Curation(DataManager(Some("myadmin"), Some("FILL.IN.YOUR@VALID-EMAIL.NL")), Some(true), None, Some(true))
  }

  it should "yield a Curation object without performed property" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")
    props.addProperty("curation.is-new-version", "yes")
    props.addProperty("curation.required", "no")
    // no curation.performed

    Curation.load(props) shouldBe Curation(DataManager(Some("myadmin"), Some("FILL.IN.YOUR@VALID-EMAIL.NL")), Some(true), Some(false), None)
  }

  val curation = Curation(DataManager(Some("x"), Some("y")), Some(true), Some(false), Some(true))

  "isNewVersionString" should "turn the isNewVersion property into a yes/no String" in {
    curation.isNewVersionString.value shouldBe "yes"
  }

  "requiredString" should "turn the required property into a yes/no String" in {
    curation.requiredString.value shouldBe "no"
  }

  "performedString" should "turn the performed property into a yes/no String" in {
    curation.performedString.value shouldBe "yes"
  }
}
