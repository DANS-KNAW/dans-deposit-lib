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

import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try

case class Identifier(doi: Doi = Doi(), fedora: FedoraId = FedoraId(), urn: Urn = Urn())
object Identifier {
  def load(properties: PropertiesConfiguration): Try[Identifier] = {
    for {
      doi <- Doi.load(properties)
      fedora = FedoraId.load(properties)
      urn = Urn.load(properties)
    } yield Identifier(doi, fedora, urn)
  }
}
