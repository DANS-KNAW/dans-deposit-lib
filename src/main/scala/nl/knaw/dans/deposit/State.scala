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

import nl.knaw.dans.deposit.DepositProperties.{ stateDescription, stateLabel }
import nl.knaw.dans.deposit.StateLabel.StateLabel
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

object StateLabel extends Enumeration {
  type StateLabel = Value

  val ARCHIVED: StateLabel = Value
  val DRAFT: StateLabel = Value
  val FAILED: StateLabel = Value
  val FINALIZING: StateLabel = Value
  val INVALID: StateLabel = Value
  val REJECTED: StateLabel = Value
  val STALLED: StateLabel = Value
  val SUBMITTED: StateLabel = Value
  val UPLOADED: StateLabel = Value
}

case class State(label: StateLabel, description: String)
object State {
  def load(properties: PropertiesConfiguration): Try[State] = {
    for {
      label <- readEnumProperty(stateLabel, StateLabel)(properties).map(_.getOrElse(missingMandatoryField(stateLabel)))
      description <- readMandatoryProperty(stateDescription)(properties)
    } yield State(label, description)
  }
}
