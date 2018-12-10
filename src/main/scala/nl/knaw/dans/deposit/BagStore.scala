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

import nl.knaw.dans.deposit.DepositProperties.{ bagStoreArchived, bagStoreBagId }
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.lang.BooleanUtils

import scala.util.{ Failure, Try }

case class BagStore(bagId: UUID, archived: Option[Boolean] = None) {
  def archivedString: Option[String] = archived.map(BooleanUtils.toStringYesNo)

  def isArchived: Boolean = archived.getOrElse(false)
}
object BagStore {
  def load(properties: PropertiesConfiguration): Try[BagStore] = {
    for {
      bagIdStr <- readMandatoryProperty(bagStoreBagId)(properties)
      bagId <- Try { UUID.fromString(bagIdStr) }.recoverWith {
        case IllegalArgumentException(msg, cause) =>
          Failure(new IllegalArgumentException(msg + s" in field '$bagStoreBagId'", cause))
      }
      archived = Option(properties.getString(bagStoreArchived)).map(BooleanUtils.toBoolean)
    } yield BagStore(bagId, archived)
  }
}
