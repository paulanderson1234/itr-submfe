/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import java.text.NumberFormat

import models.{AddressModel, ContactDetailsModel}

import scala.util.{Failure, Success, Try}

object Transformers {

  val stringToBigDecimal: String => BigDecimal = (input) => Try(BigDecimal(input.trim)) match {
    case Success(value) => value
    case Failure(_) => BigDecimal(0)
  }

  val bigDecimalToString: BigDecimal => String = (input) => input.scale match {
    case 1 => input.setScale(2).toString()
    case _ => input.toString
  }

  val stringToInteger: String => Int = (input) => Try(input.trim.toInt) match {
    case Success(value) => value
    case Failure(_) => 0
  }

  val stringToBoolean: String => Boolean = {
    case "Yes" => true
    case _ => false
  }

  val booleanToString: Boolean => String = (input) => if (input) "Yes" else "No"

  val numberToFormattedNumber: AnyVal => String = {
    (value) => value match {
      case value: Int => NumberFormat.getNumberInstance.format(value)
      case value: Long => NumberFormat.getNumberInstance.format(value)
      case _ => "N/A"
    }
  }


  val addressModelToFlattenedArray : AddressModel => Array[String] = {
    (contactAddress) => Array(Option(contactAddress.addressline1),Option(contactAddress.addressline2),contactAddress.addressline3,
      contactAddress.addressline4,contactAddress.postcode,Option(contactAddress.countryCode)).flatten
  }

  val contactDetailsModelToFlattenedArray : ContactDetailsModel => Array[String] = {
    (contactDetails) => Array(Option(contactDetails.fullName),contactDetails.telephoneNumber, contactDetails.mobileNumber,
      Option(contactDetails.email)).flatten
  }

  def poundToPence(pounds: Either[String, Int]): String = {
    pounds match {
      case Left(pounds) => pounds ++ "00"
      case Right(pounds) => (pounds * 100).toString
    }
  }
}
