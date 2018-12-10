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

class SpringfieldSpec extends TestSupportFixture {

  "load" should "yield a Springfield object with domain, user, collection and playMode present" in {
    val domain = "mydomain"
    val user = "myname"
    val collection = "my-test-files"
    val props = new PropertiesConfiguration()
    props.addProperty("springfield.domain", domain)
    props.addProperty("springfield.user", user)
    props.addProperty("springfield.collection", collection)
    props.addProperty("springfield.playmode", "continuous")

    Springfield.load(props) should matchPattern { case Success(Springfield(Some(`domain`), Some(`user`), Some(`collection`), Some(SpringfieldPlayMode.CONTINUOUS))) => }
  }

  it should "yield a Springfield object with domain not present" in {
    val user = "myname"
    val collection = "my-test-files"
    val props = new PropertiesConfiguration()
    // no springfield.domain
    props.addProperty("springfield.user", user)
    props.addProperty("springfield.collection", collection)
    props.addProperty("springfield.playmode", "continuous")

    Springfield.load(props) should matchPattern { case Success(Springfield(None, Some(`user`), Some(`collection`), Some(SpringfieldPlayMode.CONTINUOUS))) => }
  }

  it should "yield a Springfield object with no user present" in {
    val domain = "mydomain"
    val collection = "my-test-files"
    val props = new PropertiesConfiguration()
    props.addProperty("springfield.domain", domain)
    // springfield.user
    props.addProperty("springfield.collection", collection)
    props.addProperty("springfield.playmode", "continuous")

    Springfield.load(props) should matchPattern { case Success(Springfield(Some(`domain`), None, Some(`collection`), Some(SpringfieldPlayMode.CONTINUOUS))) => }
  }

  it should "yield a Springfield object with collection not present" in {
    val domain = "mydomain"
    val user = "myname"
    val props = new PropertiesConfiguration()
    props.addProperty("springfield.domain", domain)
    props.addProperty("springfield.user", user)
    // no springfield.collection
    props.addProperty("springfield.playmode", "continuous")

    Springfield.load(props) should matchPattern { case Success(Springfield(Some(`domain`), Some(`user`), None, Some(SpringfieldPlayMode.CONTINUOUS))) => }
  }

  it should "yield a Springfield object with playMode not present" in {
    val domain = "mydomain"
    val user = "myname"
    val collection = "my-test-files"
    val props = new PropertiesConfiguration()
    props.addProperty("springfield.domain", domain)
    props.addProperty("springfield.user", user)
    props.addProperty("springfield.collection", collection)

    Springfield.load(props) should matchPattern { case Success(Springfield(Some(`domain`), Some(`user`), Some(`collection`), None)) => }
  }

  it should "fail when playMode has an invalid value" in {
    val domain = "mydomain"
    val user = "myname"
    val collection = "my-test-files"
    val props = new PropertiesConfiguration()
    props.addProperty("springfield.domain", domain)
    props.addProperty("springfield.user", user)
    props.addProperty("springfield.collection", collection)
    props.addProperty("springfield.playmode", "invalid value")

    Springfield.load(props) should matchPattern {
      case Failure(NoSuchElementException("No value found for 'invalid value' in field 'springfield.playmode'", _)) =>
    }
  }
}
