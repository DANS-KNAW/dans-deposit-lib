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

import nl.knaw.dans.deposit.CurrentIngestStep.CurrentIngestStep
import nl.knaw.dans.deposit.DepositProperties.ingestCurrentStep
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

object CurrentIngestStep extends Enumeration {
  type CurrentIngestStep = Value

  val VALIDATE: CurrentIngestStep = Value
  val PID_GENERATOR: CurrentIngestStep = Value
  val FEDORA: CurrentIngestStep = Value
  val SPRINGFIELD: CurrentIngestStep = Value
  val BAGSTORE: CurrentIngestStep = Value
  val BAGINDEX: CurrentIngestStep = Value
  val SOLR4FILES: CurrentIngestStep = Value
}

case class Ingest(currentStep: Option[CurrentIngestStep] = None)
object Ingest {
  def load(properties: PropertiesConfiguration): Try[Ingest] = {
    for {
      currentStep <- readEnumProperty(ingestCurrentStep, CurrentIngestStep)(properties)
    } yield Ingest(currentStep)
  }
}
