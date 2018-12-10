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

import nl.knaw.dans.deposit.DepositProperties.{ curationPerformed, curationRequired, isNewVersion }
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.lang.BooleanUtils

case class Curation(dataManager: DataManager = DataManager(),
                    isNewVersion: Option[Boolean] = Option.empty,
                    required: Option[Boolean] = Option.empty,
                    performed: Option[Boolean] = Option.empty) {
  def isNewVersionString: Option[String] = isNewVersion.map(BooleanUtils.toStringYesNo)

  def requiredString: Option[String] = required.map(BooleanUtils.toStringYesNo)

  def performedString: Option[String] = performed.map(BooleanUtils.toStringYesNo)
}
object Curation {
  def load(properties: PropertiesConfiguration): Curation = {
    Curation(
      dataManager = DataManager.load(properties),
      isNewVersion = Option(properties.getString(isNewVersion)).map(BooleanUtils.toBoolean),
      required = Option(properties.getString(curationRequired)).map(BooleanUtils.toBoolean),
      performed = Option(properties.getString(curationPerformed)).map(BooleanUtils.toBoolean)
    )
  }
}
