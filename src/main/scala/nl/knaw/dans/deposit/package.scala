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
package nl.knaw.dans

import org.apache.commons.configuration.PropertiesConfiguration
import org.joda.time.format.ISODateTimeFormat

import scala.util.{ Failure, Try }

package object deposit {

  private[deposit] val dateTimeFormatter = ISODateTimeFormat.dateTime().withOffsetParsed()

  private[deposit] def missingMandatoryField[T](field: String): T = {
    throw new IllegalArgumentException(s"could not find mandatory field '$field'")
  }

  private[deposit] def readMandatoryProperty(field: String)(properties: PropertiesConfiguration) = {
    Try { Option(properties.getString(field)).getOrElse(missingMandatoryField(field)) }
  }

  private[deposit] def readEnumProperty[E <: Enumeration](field: String, enum: E)
                                                         (properties: PropertiesConfiguration) = {
    Try { Option(properties.getString(field)).map(enumValue => enum.withName(enumValue)) }
      .recoverWith {
        case NoSuchElementException(msg, cause) =>
          Failure(NoSuchElementException(msg + s" in field '$field'", cause))
      }
  }

  private[deposit] object NoSuchElementException {
    def apply(msg: String, cause: Throwable): Throwable = {
      new NoSuchElementException(msg).initCause(cause)
    }

    def unapply(e: NoSuchElementException): Option[(String, Throwable)] = {
      Some((e.getMessage, e.getCause))
    }
  }

  private[deposit] object IllegalArgumentException {
    def apply(msg: String, cause: Throwable): Throwable = {
      new IllegalArgumentException(msg, cause)
    }

    def unapply(e: IllegalArgumentException): Option[(String, Throwable)] = {
      Some((e.getMessage, e.getCause))
    }
  }
}
