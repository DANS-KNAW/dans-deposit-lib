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

import nl.knaw.dans.deposit.DepositProperties.sword2ContentType
import nl.knaw.dans.deposit.Sword2ContentType.Sword2ContentType
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

object Sword2ContentType extends Enumeration {
  type Sword2ContentType = Value

  val ZIP: Sword2ContentType = Value("application/zip")
  val OCTET_STREAM: Sword2ContentType = Value("application/octet-stream")
}

case class Sword2(contentType: Option[Sword2ContentType] = Option.empty)
object Sword2 {
  def load(properties: PropertiesConfiguration): Try[Sword2] = {
    for {
      contentType <- readEnumProperty(sword2ContentType, Sword2ContentType)(properties)
    } yield Sword2(contentType)
  }
}
