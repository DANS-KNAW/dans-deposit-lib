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

import nl.knaw.dans.deposit.DepositProperties.{ sprinfieldPlaymode, springfieldCollection, springfieldDomain, springfieldUser }
import nl.knaw.dans.deposit.SpringfieldPlayMode.SpringfieldPlayMode
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

object SpringfieldPlayMode extends Enumeration {
  type SpringfieldPlayMode = Value

  val CONTINUOUS: SpringfieldPlayMode = Value("continuous")
  val MENU: SpringfieldPlayMode = Value("menu")
}

case class Springfield(domain: Option[String] = Option.empty,
                       user: Option[String] = Option.empty,
                       collection: Option[String] = Option.empty,
                       playMode: Option[SpringfieldPlayMode] = Option.empty)
object Springfield {
  def load(properties: PropertiesConfiguration): Try[Springfield] = {
    for {
      playMode <- readEnumProperty(sprinfieldPlaymode, SpringfieldPlayMode)(properties)
      domain = Option(properties.getString(springfieldDomain))
      user = Option(properties.getString(springfieldUser))
      collection = Option(properties.getString(springfieldCollection))
    } yield Springfield(domain, user, collection, playMode)
  }
}
