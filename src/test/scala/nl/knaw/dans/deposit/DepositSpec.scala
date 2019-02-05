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

import java.nio.file.{ FileAlreadyExistsException, NoSuchFileException }
import java.text.SimpleDateFormat
import java.util.{ Date, UUID }

import better.files.File
import nl.knaw.dans.bag.{ ChecksumAlgorithm, RelativePath }
import nl.knaw.dans.deposit.fixtures._
import org.apache.commons.configuration.PropertiesConfiguration
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.JavaConverters._
import scala.language.{ existentials, implicitConversions, postfixOps }
import scala.util.{ Failure, Success }

class DepositSpec extends TestSupportFixture
  with TestDeposits
  with TestBags
  with FixDateTimeNow
  with Lipsum {

  "from" should "create a deposit directory named after the bagId" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = baseDir / bagId.toString
    val bagDir = depositDir / bagName

    baseDir should exist
    depositDir shouldNot exist
    bagDir shouldNot exist

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        baseDir should exist
        depositDir should exist
        bagDir should exist
        deposit.bag.baseDir shouldBe bagDir
    }
  }

  it should "create a deposit directory with a bag directory in it, equivalent to calling Bag.empty" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.bag.baseDir.listRecursively.toList should contain only(
          deposit.bag.baseDir / "data",
          deposit.bag.baseDir / "bagit.txt",
          deposit.bag.baseDir / "bag-info.txt",
          deposit.bag.baseDir / "manifest-sha512.txt",
          deposit.bag.baseDir / "tagmanifest-sha512.txt",
        )
    }
  }

  it should "create a deposit directory with a deposit.properties file in it" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.baseDir / Deposit.depositPropertiesName should exist
    }
  }

  it should "create a deposit directory with a deposit.properties file in it, containing the given, basic properties" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        val props = new PropertiesConfiguration(deposit.baseDir / Deposit.depositPropertiesName toJava)
        props.getKeys.asScala.toList should contain only(
          DepositProperties.creationTimestamp,
          DepositProperties.stateLabel,
          DepositProperties.stateDescription,
          DepositProperties.bagStoreBagId,
          DepositProperties.depositorUserId,
          DepositProperties.doiRegistered,
          DepositProperties.dansDoiAction,
        )

        props.getString(DepositProperties.creationTimestamp) shouldBe fixedDateTimeNow.toString(ISODateTimeFormat.dateTime())
        props.getString(DepositProperties.stateLabel) shouldBe state.label.toString
        props.getString(DepositProperties.stateDescription) shouldBe state.description
        props.getString(DepositProperties.bagStoreBagId) shouldBe bagId.toString
        props.getString(DepositProperties.depositorUserId) shouldBe depositor.userId
        props.getString(DepositProperties.doiRegistered) shouldBe "no"
        props.getString(DepositProperties.dansDoiAction) shouldBe "create"
    }
  }

  it should "return a Deposit object with the base directory, Bag object and DepositProperties object in it" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val baseId = UUID.randomUUID()
    val bagInfo = Map("Is-Version-Of" -> Seq(baseId.toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.creationTimestamp shouldBe fixedDateTimeNow
        deposit.stateLabel shouldBe state.label
        deposit.stateDescription shouldBe state.description
        deposit.bagId shouldBe bagId
        deposit.depositor shouldBe depositor.userId

        deposit.bag.baseDir shouldBe baseDir / bagId.toString / bagName

        deposit.bag.bagInfo should contain only(
          "Payload-Oxum" -> Seq("0.0"),
          // usually I would use Joda for this, but since this already has a fixed DateTime.now(),
          // I have to use another way of getting today's date
          "Bagging-Date" -> Seq(new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
          "Is-Version-Of" -> Seq(baseId.toString),
        )

        deposit.bag.payloadManifestAlgorithms should contain only ChecksumAlgorithm.SHA512
        deposit.bag.payloadManifests(ChecksumAlgorithm.SHA512) shouldBe empty

        deposit.bag.tagManifestAlgorithms should contain only ChecksumAlgorithm.SHA512
        deposit.bag.tagManifests(ChecksumAlgorithm.SHA512).keySet should contain only(
          deposit.bag / "bagit.txt",
          deposit.bag / "bag-info.txt",
          deposit.bag / "manifest-sha512.txt"
        )
    }
  }

  it should "fail when the baseDir of the deposit already exists" in {
    val baseDir = testDir / "emptyTestDeposit"
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val baseId = UUID.randomUUID()
    val bagInfo = Map("Is-Version-Of" -> Seq(baseId.toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    baseDir shouldNot exist

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Failure(e: NoSuchFileException) =>
        e should have message baseDir.toString()
        baseDir shouldNot exist
    }
  }

  it should "fail when the depositDir already exists" in {
    val baseDir = testDir / "emptyTestDeposit" createDirectories()
    val bagName = "my-bag"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val baseId = UUID.randomUUID()
    val bagInfo = Map("Is-Version-Of" -> Seq(baseId.toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = baseDir / bagId.toString createDirectory()
    depositDir should exist

    inside(Deposit.from(baseDir, bagId, bagName, algorithms, bagInfo, state, depositor)) {
      case Failure(e: FileAlreadyExistsException) =>
        e should have message s"$depositDir: deposit needs to be created in this directory, but it already exists"
    }
  }

  def createDataDir(): (File, File, File) = {
    val baseDir = testDir / "dataDir" createDirectories()
    val file1 = baseDir / "x" createIfNotExists() writeText lipsum(1)
    val file2 = baseDir / "sub" / "y" createIfNotExists (createParents = true) writeText lipsum(2)

    (baseDir, file1, file2)
  }

  "createFromData" should "create a deposit directory named after the bagId" in {
    val (baseDir, _, _) = createDataDir()
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = baseDir / bagId.toString
    depositDir shouldNot exist
    val bagDir = depositDir / baseDir.name
    bagDir shouldNot exist
    val depositProperties = depositDir / Deposit.depositPropertiesName
    depositProperties shouldNot exist

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        depositDir should exist
        deposit.baseDir shouldBe depositDir

        bagDir should exist
        deposit.bag.baseDir shouldBe bagDir

        depositProperties should exist

        deposit.baseDir.list.toList should contain only(
          bagDir,
          depositProperties,
        )
    }
  }

  it should "create a deposit directory with a bag directory containing a data directory with " +
    "all data that was in the given base directory" in {
    val (baseDir, file1, file2) = createDataDir()
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.baseDir shouldBe baseDir / bagId.toString
        val bagDir = baseDir / bagId.toString / baseDir.name
        bagDir should exist
        val dataDir = bagDir / "data"
        dataDir.listRecursively.filter(_.isRegularFile).toList should contain only(
          dataDir / baseDir.relativize(file1).toString,
          dataDir / baseDir.relativize(file2).toString,
        )
    }
  }

  it should "create a deposit directory with a deposit.properties file in it" in {
    val (baseDir, _, _) = createDataDir()
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.baseDir / Deposit.depositPropertiesName should exist
    }
  }

  it should "create a deposit directory with a deposit.properties file in it, containing the given, basic properties" in {
    val (baseDir, _, _) = createDataDir()
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        val props = new PropertiesConfiguration(deposit.baseDir / Deposit.depositPropertiesName toJava)
        props.getKeys.asScala.toList should contain only(
          DepositProperties.creationTimestamp,
          DepositProperties.stateLabel,
          DepositProperties.stateDescription,
          DepositProperties.bagStoreBagId,
          DepositProperties.depositorUserId,
          DepositProperties.doiRegistered,
          DepositProperties.dansDoiAction,
        )

        props.getString(DepositProperties.creationTimestamp) shouldBe fixedDateTimeNow.toString(ISODateTimeFormat.dateTime())
        props.getString(DepositProperties.stateLabel) shouldBe state.label.toString
        props.getString(DepositProperties.stateDescription) shouldBe state.description
        props.getString(DepositProperties.bagStoreBagId) shouldBe bagId.toString
        props.getString(DepositProperties.depositorUserId) shouldBe depositor.userId
        props.getString(DepositProperties.doiRegistered) shouldBe "no"
        props.getString(DepositProperties.dansDoiAction) shouldBe "create"
    }
  }

  it should "return a Deposit object with the base directory, Bag object and DepositProperties object in it" in {
    val (baseDir, file1, file2) = createDataDir()
    val file1Sha256 = file1.sha512.toLowerCase
    val file2Sha256 = file2.sha512.toLowerCase
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val baseId = UUID.randomUUID()
    val bagInfo = Map("Is-Version-Of" -> Seq(baseId.toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Success(deposit) =>
        deposit.creationTimestamp shouldBe fixedDateTimeNow
        deposit.stateLabel shouldBe state.label
        deposit.stateDescription shouldBe state.description
        deposit.bagId shouldBe bagId
        deposit.depositor shouldBe depositor.userId

        deposit.bag.baseDir shouldBe baseDir / bagId.toString / baseDir.name

        deposit.bag.bagInfo should contain only(
          "Payload-Oxum" -> Seq("1471.2"),
          // usually I would use Joda for this, but since this already has a fixed DateTime.now(),
          // I have to use another way of getting today's date
          "Bagging-Date" -> Seq(new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
          "Is-Version-Of" -> Seq(baseId.toString),
        )

        deposit.bag.payloadManifestAlgorithms should contain only ChecksumAlgorithm.SHA512
        deposit.bag.payloadManifests(ChecksumAlgorithm.SHA512) should contain only(
          deposit.bag.data / baseDir.relativize(file1).toString -> file1Sha256,
          deposit.bag.data / baseDir.relativize(file2).toString -> file2Sha256,
        )

        deposit.bag.tagManifestAlgorithms should contain only ChecksumAlgorithm.SHA512
        deposit.bag.tagManifests(ChecksumAlgorithm.SHA512).keySet should contain only(
          deposit.bag / "bagit.txt",
          deposit.bag / "bag-info.txt",
          deposit.bag / "manifest-sha512.txt"
        )
    }
  }

  it should "fail when the payloadDir does not yet exists" in {
    val baseDir = testDir / "non-existing-directory"
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    baseDir shouldNot exist

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Failure(e: NoSuchFileException) =>
        e should have message baseDir.toString()
    }
  }

  it should "fail when the depositDir already exists" in {
    val (baseDir, _, _) = createDataDir()
    val algorithms = Set(ChecksumAlgorithm.SHA512)
    val bagInfo = Map("Is-Version-Of" -> Seq(UUID.randomUUID().toString))
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = baseDir / bagId.toString createDirectory()
    depositDir should exist

    inside(Deposit.createFromData(baseDir, bagId, algorithms, bagInfo, state, depositor)) {
      case Failure(e: FileAlreadyExistsException) =>
        e should have message s"$depositDir: deposit needs to be created in this directory, but it already exists"
    }
  }

  "createFromBag" should "create a deposit directory from an existing bag, named after the bagId" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = src / bagId.toString
    depositDir shouldNot exist
    val bagDir = depositDir / src.name
    bagDir shouldNot exist
    val depositProperties = depositDir / Deposit.depositPropertiesName
    depositProperties shouldNot exist

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Success(deposit) =>
        depositDir should exist
        deposit.baseDir shouldBe depositDir

        bagDir should exist
        deposit.bag.baseDir shouldBe bagDir

        depositProperties should exist

        deposit.baseDir.list.toList should contain only(
          bagDir,
          depositProperties,
        )
    }
  }

  it should "create a deposit directory from an existing bag, containing a data directory with " +
    "all data that was in the given bag directory, as well as containing all tag files" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Success(deposit) =>
        val bagDir = deposit.baseDir / src.name
        bagDir should exist
        bagDir.listRecursively.filter(_.isRegularFile).toList should contain only(
          bagDir / "data" / "x",
          bagDir / "data" / "y",
          bagDir / "data" / "z",
          bagDir / "data" / "sub" / "u",
          bagDir / "data" / "sub" / "v",
          bagDir / "data" / "sub" / "w",
          bagDir / "metadata" / "dataset.xml",
          bagDir / "metadata" / "files.xml",
          bagDir / "bagit.txt",
          bagDir / "bag-info.txt",
          bagDir / "manifest-sha1.txt",
          bagDir / "tagmanifest-sha1.txt",
        )
    }
  }

  it should "create a deposit directory with a deposit.properties file in it" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Success(deposit) =>
        deposit.baseDir / Deposit.depositPropertiesName should exist
    }
  }

  it should "create a deposit directory with a deposit.properties file in it, containing the given, basic properties" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Success(deposit) =>
        val props = new PropertiesConfiguration(deposit.baseDir / Deposit.depositPropertiesName toJava)
        props.getKeys.asScala.toList should contain only(
          DepositProperties.creationTimestamp,
          DepositProperties.stateLabel,
          DepositProperties.stateDescription,
          DepositProperties.bagStoreBagId,
          DepositProperties.depositorUserId,
          DepositProperties.doiRegistered,
          DepositProperties.dansDoiAction,
        )

        props.getString(DepositProperties.creationTimestamp) shouldBe fixedDateTimeNow.toString(ISODateTimeFormat.dateTime())
        props.getString(DepositProperties.stateLabel) shouldBe state.label.toString
        props.getString(DepositProperties.stateDescription) shouldBe state.description
        props.getString(DepositProperties.bagStoreBagId) shouldBe bagId.toString
        props.getString(DepositProperties.depositorUserId) shouldBe depositor.userId
        props.getString(DepositProperties.doiRegistered) shouldBe "no"
        props.getString(DepositProperties.dansDoiAction) shouldBe "create"
    }
  }

  it should "return a Deposit object with the base directory, Bag object and DepositProperties object in it" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val xSha1 = (src / "data" / "x").sha1.toLowerCase
    val ySha1 = (src / "data" / "y").sha1.toLowerCase
    val zSha1 = (src / "data" / "z").sha1.toLowerCase
    val subUSha1 = (src / "data" / "sub" / "u").sha1.toLowerCase
    val subVSha1 = (src / "data" / "sub" / "v").sha1.toLowerCase
    val subWSha1 = (src / "data" / "sub" / "w").sha1.toLowerCase
    val datasetXmlSha1 = (src / "metadata" / "dataset.xml").sha1.toLowerCase
    val filesXmlSha1 = (src / "metadata" / "files.xml").sha1.toLowerCase
    val bagitSha1 = (src / "bagit.txt").sha1.toLowerCase
    val bagInfoSha1 = (src / "bag-info.txt").sha1.toLowerCase
    val manifestSha1Sha1 = (src / "manifest-sha1.txt").sha1.toLowerCase

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Success(deposit) =>
        deposit.creationTimestamp shouldBe fixedDateTimeNow
        deposit.stateLabel shouldBe state.label
        deposit.stateDescription shouldBe state.description
        deposit.bagId shouldBe bagId
        deposit.depositor shouldBe depositor.userId

        deposit.bag.baseDir shouldBe src / bagId.toString / src.name

        deposit.bag.bagInfo should contain only(
          "Payload-Oxum" -> Seq("72.6"),
          "Bagging-Date" -> Seq("2016-06-07"),
          "Created" -> Seq("2017-01-16T14:35:00.888+01:00"),
          "Bag-Size" -> Seq("0.6 KB"),
        )

        deposit.bag.payloadManifestAlgorithms should contain only ChecksumAlgorithm.SHA1
        deposit.bag.payloadManifests(ChecksumAlgorithm.SHA1) should contain only(
          deposit.bag / "data" / "x" -> xSha1,
          deposit.bag / "data" / "y" -> ySha1,
          deposit.bag / "data" / "z" -> zSha1,
          deposit.bag / "data" / "sub" / "u" -> subUSha1,
          deposit.bag / "data" / "sub" / "v" -> subVSha1,
          deposit.bag / "data" / "sub" / "w" -> subWSha1,
        )

        deposit.bag.tagManifestAlgorithms should contain only ChecksumAlgorithm.SHA1
        deposit.bag.tagManifests(ChecksumAlgorithm.SHA1) should contain only(
          deposit.bag / "metadata" / "dataset.xml" -> datasetXmlSha1,
          deposit.bag / "metadata" / "files.xml" -> filesXmlSha1,
          deposit.bag / "bagit.txt" -> bagitSha1,
          deposit.bag / "bag-info.txt" -> bagInfoSha1,
          deposit.bag / "manifest-sha1.txt" -> manifestSha1Sha1,
        )
    }
  }

  it should "fail when the payloadDir does not yet exists" in {
    val src = testDir / "non-existing-directory"
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    src shouldNot exist

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Failure(e: NoSuchFileException) =>
        e should have message src.toString()
    }
  }

  it should "fail when the depositDir to be created already exists" in {
    val src = simpleBagDirV0
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    val depositDir = src / bagId.toString createDirectory()

    inside(Deposit.createFromBag(src, bagId, state, depositor)) {
      case Failure(e: FileAlreadyExistsException) =>
        e should have message s"$depositDir: deposit needs to be created in this directory, but it already exists"
    }
  }

  it should "fail when the input bag is not a bag" in {
    val (baseDir, _, _) = createDataDir()
    val state = State(StateLabel.DRAFT, "deposit under construction")
    val depositor = Depositor("ikke")
    val bagId = UUID.randomUUID()

    inside(Deposit.createFromBag(baseDir, bagId, state, depositor)) {
      case Failure(e: NoSuchFileException) =>
        e should have message (baseDir / bagId.toString / baseDir.name / "bagit.txt").toString()
    }
  }

  "read" should "load a valid deposit on filesystem into a Deposit object" in {
    val deposit = simpleDepositV0()

    // this method is actually tested in all other methods, since it is used to read all test bags
    // therefore just a single simple check here
    deposit.baseDir shouldBe simpleDepositDirV0
  }

  it should "fail when a deposit does not contain a directory (which might classify as a bag)" in {
    val depositDir = simpleDepositDirV0
    val bagDir = depositDir / "bag"

    bagDir should exist
    bagDir.delete()
    bagDir shouldNot exist

    inside(Deposit.read(depositDir)) {
      case Failure(e: IllegalArgumentException) =>
        e should have message s"$depositDir is not a deposit: it contains no directories"
    }
  }

  it should "fail when a deposit contains multiple directories (which might classify as a bag)" in {
    val depositDir = simpleDepositDirV0
    depositDir / "other-directory" createDirectory()

    depositDir.list.count(_.isDirectory) shouldBe 2

    inside(Deposit.read(depositDir)) {
      case Failure(e: IllegalArgumentException) =>
        e should have message s"$depositDir is not a deposit: it contains multiple directories"
    }
  }

  "baseDir" should "return the deposit's base directory" in {
    val deposit = simpleDepositV0()

    deposit.baseDir shouldBe simpleDepositDirV0
  }

  "bag" should "return the Deposit's bag object" in {
    val deposit = simpleDepositV0()

    deposit.bag.baseDir shouldBe simpleDepositDirV0 / "bag"
  }

  "creationTimestamp" should "return the creation timestamp from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.creationTimestamp.getMillis shouldBe new DateTime(2018, 5, 25, 20, 8, 56, 210, DateTimeZone.forOffsetHoursMinutes(2, 0)).getMillis
  }

  "withCreationTimestamp" should "change the creation timestamp and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val timestamp = fixedDateTimeNow

    val resultDeposit = deposit.withCreationTimestamp(timestamp)

    resultDeposit.creationTimestamp shouldBe timestamp
  }

  "stateLabel" should "return the state label from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.stateLabel shouldBe StateLabel.SUBMITTED
  }

  "stateDescription" should "return the state description from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.stateDescription shouldBe "Deposit is valid, and ready for post-submission processing"
  }

  "withState" should "change the state label and description and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val label = StateLabel.ARCHIVED
    val description = "deposit is now archived"

    val resultDeposit = deposit.withState(label, description)

    resultDeposit.stateLabel shouldBe label
    resultDeposit.stateDescription shouldBe description
  }

  "depositor" should "return the userId of the depositor from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.depositor shouldBe "myuser"
  }

  "withDepositor" should "change the depositor's id and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val depositor = "other-depositor"

    val resultDeposit = deposit.withDepositor(depositor)

    resultDeposit.depositor shouldBe depositor
  }

  "currentIngestStep" should "return the current ingest step property from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.currentIngestStep.value shouldBe CurrentIngestStep.FEDORA
  }

  "withCurrentIngestStep" should "change the current ingest step property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()

    val resultDeposit = deposit.withCurrentIngestStep(CurrentIngestStep.BAGSTORE)

    resultDeposit.currentIngestStep.value shouldBe CurrentIngestStep.BAGSTORE
  }

  "withoutCurrentIngestStep" should "remove the current ingest step property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutCurrentIngestStep

    resultDeposit.currentIngestStep shouldBe empty
  }

  "bagId" should "return the bagId of the deposit from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.bagId shouldBe UUID.fromString("1c2f78a1-26b8-4a40-a873-1073b9f3a56a")
  }

  "isArchived" should "return the isArchived property from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.isArchived.value shouldBe true
  }

  "withIsArchived" should "change the isArchived property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val isArchived = false

    val resultDeposit = deposit.withIsArchived(isArchived)

    resultDeposit.isArchived.value shouldBe isArchived
  }

  "withoutIsArchived" should "remove the isArchived property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsArchived

    resultDeposit.isArchived shouldBe empty
  }

  "doi" should "return the doi of this deposit from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.doi.value shouldBe "some-random-doi"
  }

  "withDoi" should "change the doi of this deposit and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val doi = "other-doi"

    val resultDeposit = deposit.withDoi(doi)

    resultDeposit.doi.value shouldBe doi
  }

  "withoutDoi" should "remove the doi of this deposit and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutDoi

    resultDeposit.doi shouldBe empty
  }

  "isDoiRegistered" should "return whether the doi is registered" in {
    val deposit = simpleDepositV0()

    deposit.isDoiRegistered.value shouldBe true
  }

  "withIsDoiRegistered" should "change the registered flag for the doi and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withIsDoiRegistered(false)

    resultDeposit.isDoiRegistered.value shouldBe false
  }

  "withoutIsDoiRegistered" should "remove the registered flag for the doi and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsDoiRegistered

    resultDeposit.isDoiRegistered shouldBe empty
  }

  "dansDoiAction" should "return the required action of the doi" in {
    val deposit = simpleDepositV0()

    deposit.dansDoiAction.value shouldBe Action.create
  }

  "withDansDoiAction" should "change the value of the dans doi action and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withDansDoiAction(Action.update)

    resultDeposit.dansDoiAction.value shouldBe Action.update
  }

  "withoutDansDoiAction" should "remove the dans doi action property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutDansDoiAction

    resultDeposit.dansDoiAction shouldBe empty
  }

  "fedoraId" should "return the fedora identifier of this deposit from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.fedoraId.value shouldBe "some-random-fedora-identifier"
  }

  "withFedoraId" should "change the fedora identifier of this deposit and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val fd = "other-fedora-identifier"

    val resultDeposit = deposit.withFedoraId(fd)

    resultDeposit.fedoraId.value shouldBe fd
  }

  "withoutFedoraId" should "remove the fedora identifier of this deposit and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutFedoraId

    resultDeposit.fedoraId shouldBe empty
  }

  "dataManagerId" should "return the datamanager's id from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.dataManagerId.value shouldBe "myadmin"
  }

  "withDataManagerId" should "change the datamanager's id and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val datamanagerId = "id"

    val resultDeposit = deposit.withDataManagerId(datamanagerId)

    resultDeposit.dataManagerId.value shouldBe datamanagerId
  }

  "withoutIsArchived" should "remove the datamanager's id and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsArchived

    resultDeposit.isArchived shouldBe empty
  }

  "dataManagerEmail" should "return the datamanager's email from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.dataManagerEmail.value shouldBe "FILL.IN.YOUR@VALID-EMAIL.NL"
  }

  "withDataManagerEmail" should "change the datamanager's email and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val datamanagerEmail = "me@email.com"

    val resultDeposit = deposit.withDataManagerEmail(datamanagerEmail)

    resultDeposit.dataManagerEmail.value shouldBe datamanagerEmail
  }

  "withoutDataManagerEmail" should "remove the datamanager's email and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutDataManagerEmail

    resultDeposit.dataManagerEmail shouldBe empty
  }

  "isNewVersion" should "return the isNewVersion property from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.isNewVersion.value shouldBe false
  }

  "withIsNewVersion" should "change the isNewVersion property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val isNewVersion = true

    val resultDeposit = deposit.withIsNewVersion(isNewVersion)

    resultDeposit.isNewVersion.value shouldBe isNewVersion
  }

  "withoutIsNewVersion" should "remove the isNewVersion property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsNewVersion

    resultDeposit.isNewVersion shouldBe empty
  }

  "isCurationRequired" should "return the isCurationRequired property from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.isCurationRequired.value shouldBe true
  }

  "withIsCurationRequired" should "change the isCurationRequired property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val required = false

    val resultDeposit = deposit.withIsCurationRequired(required)

    resultDeposit.isCurationRequired.value shouldBe required
  }

  "withoutIsCurationRequired" should "remove the isCurationRequired property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsCurationRequired

    resultDeposit.isCurationRequired shouldBe empty
  }

  "isCurationPerformed" should "return the isCurationPerformed property from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.isCurationPerformed.value shouldBe false
  }

  "withIsCurationPerformed" should "change the isCurationPerformed property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val performed = true

    val resultDeposit = deposit.withIsCurationPerformed(performed)

    resultDeposit.isCurationPerformed.value shouldBe performed
  }

  "withoutIsCurationPerformed" should "remove the isCurationPerformed property and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutIsCurationPerformed

    resultDeposit.isCurationPerformed shouldBe empty
  }

  "springfieldDomain" should "return the Springfield domain from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.springfieldDomain.value shouldBe "mydomain"
  }

  "withSpringfieldDomain" should "change the Springfield domain and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val domain = "other-domain"

    val resultDeposit = deposit.withSpringfieldDomain(domain)

    resultDeposit.springfieldDomain.value shouldBe domain
  }

  "withoutSpringfieldDomain" should "remove the Springfield domain and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutSpringfieldDomain

    resultDeposit.springfieldDomain shouldBe empty
  }

  "springfieldUser" should "return the Springfield user from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.springfieldUser.value shouldBe "myname"
  }

  "withSpringfieldUser" should "change the Springfield user and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val user = "other-user"

    val resultDeposit = deposit.withSpringfieldUser(user)

    resultDeposit.springfieldUser.value shouldBe user
  }

  "withoutSpringfieldUser" should "remove the Springfield user and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutSpringfieldUser

    resultDeposit.springfieldUser shouldBe empty
  }

  "springfieldCollection" should "return the Springfield collection from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.springfieldCollection.value shouldBe "my-test-files"
  }

  "withSpringfieldCollection" should "change the Springfield collection and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val collection = "other-collection"

    val resultDeposit = deposit.withSpringfieldCollection(collection)

    resultDeposit.springfieldCollection.value shouldBe collection
  }

  "withoutSpringfieldCollection" should "remove the Springfield collection and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutSpringfieldCollection

    resultDeposit.springfieldCollection shouldBe empty
  }

  "springfieldPlayMode" should "return the Springfield playmode from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.springfieldPlayMode.value shouldBe SpringfieldPlayMode.CONTINUOUS
  }

  "withSpringfieldPlayMode" should "change the Springfield playmode and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val playMode = SpringfieldPlayMode.MENU

    val resultDeposit = deposit.withSpringfieldPlayMode(playMode)

    resultDeposit.springfieldPlayMode.value shouldBe playMode
  }

  "withoutSpringfieldPlayMode" should "remove the Springfield playmode and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutSpringfieldPlayMode

    resultDeposit.springfieldPlayMode shouldBe empty
  }

  "stageState" should "return the stage state from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.stageState.value shouldBe StageState.ARCHIVED
  }

  "withStageState" should "change the stage state and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val stageState = StageState.DRAFT

    val resultDeposit = deposit.withStageState(stageState)

    resultDeposit.stageState.value shouldBe stageState
  }

  "withoutStageState" should "remove the stage state and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutStageState

    resultDeposit.stageState shouldBe empty
  }

  "sword2ContentType" should "return the sword2 contentType from deposit.properties" in {
    val deposit = simpleDepositV0()

    deposit.sword2ContentType.value shouldBe Sword2ContentType.OCTET_STREAM
  }

  "withSword2ContentType" should "change the sword contentType and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val contentType = Sword2ContentType.ZIP

    val resultDeposit = deposit.withSword2ContentType(contentType)

    resultDeposit.sword2ContentType.value shouldBe contentType
  }

  "withoutSword2ContentType" should "remove the sword contentType and return the new DepositProperties" in {
    val deposit = simpleDepositV0()
    val resultDeposit = deposit.withoutSword2ContentType

    resultDeposit.sword2ContentType shouldBe empty
  }

  "save" should "write changes made to the Bag object in the bag to the deposit on file system" in {
    val deposit = simpleDepositV0()
    val newFile = testDir / "new-file" writeText lipsum(5)
    val relativeDest: RelativePath = _ / "abc.txt"
    val dest = relativeDest(deposit.bag.data)
    val sha1Checksum = newFile.sha1.toLowerCase

    dest shouldNot exist
    deposit.bag.payloadManifestAlgorithms should contain only ChecksumAlgorithm.SHA1
    deposit.bag.payloadManifests(ChecksumAlgorithm.SHA1) shouldNot contain key dest

    deposit.bag
      .addPayloadFile(newFile)(_ / "abc.txt")
      .flatMap(_.addPayloadManifestAlgorithm(ChecksumAlgorithm.MD5)) shouldBe a[Success[_]]

    // without saving, the new file should be added to the bag...
    dest should exist
    // ... and all object structures should be updated accordingly...
    deposit.bag.payloadManifestAlgorithms should contain only(ChecksumAlgorithm.SHA1, ChecksumAlgorithm.MD5)
    deposit.bag.payloadManifests(ChecksumAlgorithm.SHA1) should contain key dest
    deposit.bag.payloadManifests(ChecksumAlgorithm.MD5) should contain key dest
    // ... however, they are not yet persisted to file
    (deposit.bag.baseDir / "manifest-sha1.txt").contentAsString should (
      not include deposit.bag.baseDir.relativize(dest).toString and
        not include sha1Checksum)
    deposit.bag.baseDir / "manifest-md5.txt" shouldNot exist

    deposit.save() shouldBe a[Success[_]]

    (deposit.bag.baseDir / "manifest-sha1.txt").contentAsString should (
      include(deposit.bag.baseDir.relativize(dest).toString) and
        include(sha1Checksum))
    deposit.bag.baseDir / "manifest-md5.txt" should exist
  }

  it should "write changes made to the DepositProperties to the deposit on file system" in {
    val stateLabel = StateLabel.ARCHIVED
    val stateDescription = "deposit is archived"
    val doi = "0123456789abcdef"

    simpleDepositV0()
      .withState(stateLabel, stateDescription)
      .withDoi(doi)
      .save() shouldBe a[Success[_]]

    val deposit = simpleDepositV0()

    deposit.stateLabel shouldBe stateLabel
    deposit.stateDescription shouldBe stateDescription
    deposit.doi.value shouldBe doi
  }
}
