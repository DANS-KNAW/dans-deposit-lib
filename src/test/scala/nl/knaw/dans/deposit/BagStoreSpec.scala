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

import java.util.UUID

import nl.knaw.dans.deposit.fixtures.TestSupportFixture
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.{ Failure, Success }

class BagStoreSpec extends TestSupportFixture {

  "load" should "yield a BagStore object with bagId and archived present" in {
    val uuid = UUID.randomUUID()
    val props = new PropertiesConfiguration()
    props.addProperty("bag-store.bag-id", uuid.toString)
    props.addProperty("bag-store.archived", "yes")

    BagStore.load(props) should matchPattern { case Success(BagStore(`uuid`, Some(true))) => }
  }

  it should "yield a BagStore object with only bagId present" in {
    val uuid = UUID.randomUUID()
    val props = new PropertiesConfiguration()
    props.addProperty("bag-store.bag-id", uuid.toString)

    BagStore.load(props) should matchPattern { case Success(BagStore(`uuid`, None)) => }
  }

  it should "fail when bagId is not present" in {
    val props = new PropertiesConfiguration()
    // no bag-store.bag-id
    props.addProperty("bag-store.archived", "yes")

    inside(BagStore.load(props)) {
      case Failure(e: IllegalArgumentException) =>
        e should have message "could not find mandatory field 'bag-store.bag-id'"
    }
  }

  it should "fail when bagId is not a UUID" in {
    val props = new PropertiesConfiguration()
    props.addProperty("bag-store.bag-id", "not a UUID")
    props.addProperty("bag-store.archived", "yes")

    inside(BagStore.load(props)) {
      case Failure(e: IllegalArgumentException) =>
        e should have message "Invalid UUID string: not a UUID in field 'bag-store.bag-id'"
    }
  }

  it should "yield a BagStore object with archived = false when this property has an invalid value" in {
    val uuid = UUID.randomUUID()
    val props = new PropertiesConfiguration()
    props.addProperty("bag-store.bag-id", uuid.toString)
    props.addProperty("bag-store.archived", "invalid value")

    BagStore.load(props) should matchPattern { case Success(BagStore(`uuid`, Some(false))) => }
  }

  "archivedString" should "convert the archived field true back to a string" in {
    BagStore(UUID.randomUUID(), Option(true)).archivedString.value shouldBe "yes"
  }

  it should "convert the archived field false back to a string" in {
    BagStore(UUID.randomUUID(), Option(false)).archivedString.value shouldBe "no"
  }

  it should "not convert an unpresent archived field" in {
    BagStore(UUID.randomUUID(), None).archivedString shouldBe empty
  }

  "isArchived" should "return true when archived = Some(true)" in {
    BagStore(UUID.randomUUID(), Option(true)).isArchived shouldBe true
  }

  it should "return false when archived = Some(false)" in {
    BagStore(UUID.randomUUID(), Option(false)).isArchived shouldBe false
  }

  it should "return false when archived = None" in {
    BagStore(UUID.randomUUID(), None).isArchived shouldBe false
  }
}
