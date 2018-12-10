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

import nl.knaw.dans.deposit.Action.Action
import nl.knaw.dans.deposit.DepositProperties.{ dansDoiAction, doiIdentifier, doiRegistered }
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.lang.BooleanUtils

import scala.util.Try

object Action extends Enumeration {
  type Action = Value

  val create: Action = Value
  val update: Action = Value
}

case class Doi(value: Option[String] = None,
               registered: Option[Boolean] = Some(false),
               action: Option[Action] = Some(Action.create)) {

  def registeredString: Option[String] = registered.map(BooleanUtils.toStringYesNo)
}
object Doi {
  def load(properties: PropertiesConfiguration): Try[Doi] = {
    for {
      action <- readEnumProperty(dansDoiAction, Action)(properties)
      value = Option(properties.getString(doiIdentifier))
      registered = Option(properties.getString(doiRegistered)).map(BooleanUtils.toBoolean)
    } yield Doi(value, registered, action)
  }
}
