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

import java.nio.file.NoSuchFileException

import better.files.File
import nl.knaw.dans.deposit.DepositProperties._
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.{ Failure, Try }

case class DepositProperties(creation: Creation = Creation(),
                             state: State,
                             depositor: Depositor,
                             ingest: Ingest = Ingest(),
                             bagStore: BagStore,
                             identifier: Identifier = Identifier(),
                             curation: Curation = Curation(),
                             springfield: Springfield = Springfield(),
                             staged: Staged = Staged()) {

  /**
   * Writes the `DepositProperties` to `file` on the filesystem.
   *
   * @param file the file location to serialize the properties to
   * @return `scala.util.Success` if the save was performed successfully,
   *         `scala.util.Failure` otherwise
   */
  def save(file: File): Try[Unit] = Try {
    new PropertiesConfiguration {
      setDelimiterParsingDisabled(true)

      setProperty(creationTimestamp, creation.timestampString)

      setProperty(stateLabel, state.label.toString)
      setProperty(stateDescription, state.description)

      setProperty(depositorUserId, depositor.userId)

      ingest.currentStep.foreach(step => setProperty(ingestCurrentStep, step.toString))

      setProperty(bagStoreBagId, bagStore.bagId)
      bagStore.archivedString.foreach(setProperty(bagStoreArchived, _))

      identifier.doi.value.foreach(setProperty(doiIdentifier, _))
      identifier.doi.registeredString.foreach(setProperty(doiRegistered, _))
      identifier.doi.action.foreach(setProperty(dansDoiAction, _))
      identifier.fedora.value.foreach(setProperty(fedoraIdentifier, _))

      curation.dataManager.userId.foreach(setProperty(dataManagerUserId, _))
      curation.dataManager.email.foreach(setProperty(datamanagerEmail, _))
      curation.isNewVersionString.foreach(setProperty(isNewVersion, _))
      curation.requiredString.foreach(setProperty(curationRequired, _))
      curation.performedString.foreach(setProperty(curationPerformed, _))

      springfield.domain.foreach(setProperty(springfieldDomain, _))
      springfield.user.foreach(setProperty(springfieldUser, _))
      springfield.collection.foreach(setProperty(springfieldCollection, _))
      springfield.playMode.foreach(setProperty(sprinfieldPlaymode, _))

      staged.state.foreach(state => setProperty(stagedState, state.toString))
    }.save(file.toJava)
  }
}

object DepositProperties {

  // @formatter:off
  val creationTimestamp     = "creation.timestamp"
  val stateLabel            = "state.label"
  val stateDescription      = "state.description"
  val depositorUserId       = "depositor.userId"
  val ingestCurrentStep     = "deposit.ingest.current-step"
  val bagStoreBagId         = "bag-store.bag-id"
  val bagStoreArchived      = "bag-store.archived"
  val doiIdentifier         = "identifier.doi"
  val dansDoiAction         = "identifier.dans-doi.action"
  val doiRegistered         = "identifier.dans-doi.registered"
  val fedoraIdentifier      = "identifier.fedora"
  val dataManagerUserId     = "curation.datamanager.userId"
  val datamanagerEmail      = "curation.datamanager.email"
  val isNewVersion          = "curation.is-new-version"
  val curationRequired      = "curation.required"
  val curationPerformed     = "curation.performed"
  val springfieldDomain     = "springfield.domain"
  val springfieldUser       = "springfield.user"
  val springfieldCollection = "springfield.collection"
  val sprinfieldPlaymode    = "springfield.playmode"
  val stagedState           = "staged.state"
  // @formatter:on

  /**
   * Creates a `DepositProperties` object, with only the mandatory properties set.
   *
   * @param state     the `State` to be set
   * @param depositor the accountname of the depositor
   * @param bagStore  the bagId to be used for this deposit
   * @return a new `DepositProperties`
   */
  def from(state: State, depositor: Depositor, bagStore: BagStore): DepositProperties = {
    DepositProperties(
      state = state,
      depositor = depositor,
      bagStore = bagStore
    )
  }

  /**
   * Reads a `File` as a `deposit.properties` file.
   *
   * @param propertiesFile the file to be converted to a `DepositProperties`
   * @return if successful the `DepositProperties` representing the `propertiesFile`,
   *         else a Failure with a NoSuchFileException
   */
  def read(propertiesFile: File): Try[DepositProperties] = {
    if (propertiesFile.exists && propertiesFile.isRegularFile)
      Try {
        new PropertiesConfiguration {
          setDelimiterParsingDisabled(true)
          load(propertiesFile.toJava)
        }
      }.flatMap(load)
    else
      Failure(new NoSuchFileException(s"$propertiesFile does not exist or isn't a file"))
  }

  /**
   * Loads a new `DepositProperties` object with the corresponding elements from the
   * `PropertiesConfiguration`. `properties` should at least contain all mandatory properties.
   *
   * @param properties the `PropertiesConfiguration` containing at least all mandatory deposit properties
   * @return if successful a new `DepositProperties` representing the provided `properties`
   *         else a `Failure` with a `NoSuchElementException` if not all deposit properties were present
   */
  def load(properties: PropertiesConfiguration): Try[DepositProperties] = {
    for {
      creation <- Creation.load(properties)
      state <- State.load(properties)
      depositor <- Depositor.load(properties)
      ingest <- Ingest.load(properties)
      bagStore <- BagStore.load(properties)
      identifier <- Identifier.load(properties)
      curation = Curation.load(properties)
      springfield <- Springfield.load(properties)
      staged <- Staged.load(properties)
    } yield DepositProperties(creation, state, depositor, ingest, bagStore, identifier, curation, springfield, staged)
  }
}
