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

class DataManagerSpec extends TestSupportFixture {

  "load" should "yield a DataManager object with userId and email present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")

    DataManager.load(props) shouldBe DataManager(Some("myadmin"), Some("FILL.IN.YOUR@VALID-EMAIL.NL"))
  }

  it should "yield a DataManager object with only userId present" in {
    val props = new PropertiesConfiguration()
    props.addProperty("curation.datamanager.userId", "myadmin")
    // no curation.datamanager.email

    DataManager.load(props) shouldBe DataManager(Some("myadmin"), None)
  }

  it should "yield a DataManager object with only email present" in {
    val props = new PropertiesConfiguration()
    // no curation.datamanager.userId
    props.addProperty("curation.datamanager.email", "FILL.IN.YOUR@VALID-EMAIL.NL")

    DataManager.load(props) shouldBe DataManager(None, Some("FILL.IN.YOUR@VALID-EMAIL.NL"))
  }
}
