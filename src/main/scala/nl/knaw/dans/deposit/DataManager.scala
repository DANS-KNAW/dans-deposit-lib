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

import nl.knaw.dans.deposit.DepositProperties.{ dataManagerUserId, datamanagerEmail }
import org.apache.commons.configuration.PropertiesConfiguration

case class DataManager(userId: Option[String] = Option.empty, email: Option[String] = Option.empty)
object DataManager {
  def load(properties: PropertiesConfiguration): DataManager = {
    new DataManager(
      Option(properties.getString(dataManagerUserId)),
      Option(properties.getString(datamanagerEmail)),
    )
  }
}
