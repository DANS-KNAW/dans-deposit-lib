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

import nl.knaw.dans.deposit.DepositProperties.stagedState
import nl.knaw.dans.deposit.StageState.StageState
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

object StageState extends Enumeration {
  type StageState = Value

  val DRAFT: StageState = Value
  val FINALIZING: StageState = Value
  val INVALID: StageState = Value
  val SUBMITTED: StageState = Value
  val REJECTED: StageState = Value
  val FAILED: StageState = Value
  val STALLED: StageState = Value
  val ARCHIVED: StageState = Value
}

case class Staged(state: Option[StageState] = Option.empty)
object Staged {
  def load(properties: PropertiesConfiguration): Try[Staged] = {
    for {
      state <- readEnumProperty(stagedState, StageState)(properties)
    } yield Staged(state)
  }
}
