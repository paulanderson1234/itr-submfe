/*
 * Copyright 2016 HM Revenue & Customs
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

import java.text.{ParseException, SimpleDateFormat}

import models.{CommercialSaleModel, CompanyAddressModel, DateOfFirstSaleModel}
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation._
import play.api.i18n.Messages
import java.util.{Calendar, Date}

import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

object Validation {

  // use new Date() to get the date now
  lazy val sf = new SimpleDateFormat("dd/MM/yyyy")
  lazy val datePageFormat = new SimpleDateFormat("dd MMMM yyyy")
  lazy val datePageFormatNoZero = new SimpleDateFormat("d MMMM yyyy")

  def dateOfCommercialSaleDateValidation : Constraint[CommercialSaleModel] = {

    def validateYes(dateForm :CommercialSaleModel) = {
      anyEmpty(dateForm.day, dateForm.month, dateForm.year) match {
        case true => Invalid(Seq(ValidationError(Messages("validation.error.DateNotEntered"))))
        case false => isValidDate(dateForm.day.get, dateForm.month.get, dateForm.year.get) match {
          case false => Invalid(Seq(ValidationError(Messages("common.date.error.invalidDate"))))
          case true => dateNotInFuture(dateForm.day.get, dateForm.month.get, dateForm.year.get) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.DateOfCommercialSale.Future"))))
          }
        }
      }
    }

    Constraint("constraints.date_of_first_sale")({
      dateForm : CommercialSaleModel =>
        dateForm.hasCommercialSale match {
          case "No" => allDatesEmpty(dateForm.day, dateForm.month, dateForm.year) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.DateForNoOption"))))
          }
          case "Yes" => validateYes(dateForm)
        }
    })
  }

  def anyEmpty(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    if(day.isEmpty || month.isEmpty || year.isEmpty){
      true
    } else {
      false
    }
  }

  def allDatesEmpty(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    if(day.isEmpty & month.isEmpty & year.isEmpty){
      true
    } else {
      false
    }
  }

  def validateNonEmptyDateOptions(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    if(day.isEmpty || month.isEmpty || year.isEmpty){
      false
    } else {
      true
    }
  }

  def mandatoryAddressLineCheck: Mapping[String] = {
    val validAddressLine = """[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,26}""".r
    val addresssLineCheckConstraint: Constraint[String] =
      Constraint("contraints.mandatoryAddressLine")({
        text =>
          val error = text match {
            case validAddressLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.mandatoryaddresssline")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text.verifying(addresssLineCheckConstraint)
  }

  def optionalAddressLineCheck: Mapping[String] = {
    val validAddressLine = """^$|[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,26}""".r
    val addresssLineCheckConstraint: Constraint[String] =
      Constraint("contraints.optionalAddressLine")({
        text =>
          val error = text match {
            case validAddressLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.optionaladdresssline")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(addresssLineCheckConstraint)
  }

  def addressLineFourCheck: Mapping[String] = {
    val validAddressLine = """^$|[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,17}""".r
    val addressLineFourCheckConstraint: Constraint[String] =
      Constraint("contraints.addressLineFour")({
        text =>
          val error = text match {
            case validAddressLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.linefouraddresssline")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(addressLineFourCheckConstraint)
  }

  def postcodeCheck: Mapping[String] = {
    val validPostcodeLine = "^$|[A-Z]{1,2}[0-9][0-9A-Z]? [0-9][A-Z]{2}".r
    val postcodeCheckConstraint: Constraint[String] =
      Constraint("contraints.postcode")({
        text =>
          val error = text.toUpperCase match {
            case validPostcodeLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.postcode")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(postcodeCheckConstraint)
  }

  def postcodeLookupCheck: Mapping[String] = {
    val validPostcodeLine = "^[A-Z]{1,2}[0-9A-Z]{1,2} [0-9][A-Z]{2}$".r
    val postcodeLookupCheckConstraint: Constraint[String] =
      Constraint("contraints.postcode")({
        text =>
          val error = text.toUpperCase match {
            case validPostcodeLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.postcodelookup")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(postcodeLookupCheckConstraint)
  }

  def countryCheck: Mapping[String] = {
    val validCountryLine = "^$|[A-Za-z0-9]{1}[A-Za-z 0-9]{0,19}".r
    val countryCheckConstraint: Constraint[String] =
      Constraint("contraints.country")({
        text =>
          val error = text match {
            case validCountryLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.country")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(countryCheckConstraint)
  }

  def emailCheck: Mapping[String] = {
    val validEmailLine = """[A-Za-z0-9\-​_.]{1,64}@[A-Za-z0-9\-_​.]{1,64}""".r
    val emailCheckConstraint: Constraint[String] =
      Constraint("contraints.email")({
        text =>
          val error = text match {
            case validEmailLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.email")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(emailCheckConstraint)
  }

  def postcodeCountryCheckConstraint: Constraint[CompanyAddressModel] = {
    Constraint("constraints.postcodeCountryCheck")({
      companyAddressForm: CompanyAddressModel =>
        if (companyAddressForm.country.length > 0 && companyAddressForm.postcode.length > 0) {
          Invalid(Seq(ValidationError(Messages("validation.error.countrypostcode"))))
        } else {
          Valid
        }
    })
  }

  def crnCheck: Mapping[String] = {
    val validcrn = """[\d]{8}|[A-Za-z]{2}[\d]{6}""".r
    val crnCheckConstraint: Constraint[String] =
      Constraint("contraints.crn")({
        text =>
          val error = text match {
            case validcrn() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.crn")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(crnCheckConstraint)
  }

  def utrTenCharCheck: Mapping[String] = {
    val validUtr = """[0-9]{10}""".r
    val utrCharCheckConstraint: Constraint[String] =
      Constraint("contraints.utrTen")({
        text =>
          val error = text match {
            case validUtr() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.utrTenChar")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(utrCharCheckConstraint)
  }


  def isValidDateOptions(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    validateNonEmptyDateOptions(day, month, year) match {
      case  false => true
      case _ => isValidDate(day.get, month.get, year.get)
    }
  }

  def isValidDate(day: Int, month: Int, year: Int): Boolean = {
    Try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"$day/$month/$year")
      year match {
        case year if year < 1000 => false
        case _ => true
      }
    } match {
      case Success(result) => result
      case Failure(_) => false
    }
  }

  def constructDate (day: Int, month: Int, year: Int): Date = {
    sf.parse(s"$day/$month/$year")
  }

  def dateInFuture (date: Date): Boolean = {
    date.after(DateTime.now.toDate)
  }

  /** Determines whether the date of incorporation passed is less than 3 years from today*/
  def dateAfterIncorporationRule(day:Int, month:Int, year:Int) : Boolean =
  {
    Try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"$day/$month/$year")
      constructDate(day, month, year).after(fmt.parse(dateMinusYears(Some(new Date()), 3)))
    } match {
      case Success(result) => result
      case Failure(_) => false
    }
  }

  def dateNotInFuture (day: Int, month: Int, year: Int): Boolean = {
    !constructDate(day, month, year).after(DateTime.now.toDate)
  }

  def dateIsFuture (day: Int, month: Int, year: Int): Boolean = {
    constructDate(day, month, year).after(DateTime.now.toDate)
  }

  def dateNotInFutureOptions(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    // if empty elements return as valid to prevent chaining of multiple errors (other validators should handle this)
    validateNonEmptyDateOptions(day, month, year) match {
      case  false => true
      case _ => dateNotInFuture(day.get, month.get, year.get)
    }
  }

  def dateInFutureOptions(day:Option[Int], month:Option[Int], year:Option[Int]) : Boolean = {
    // if empty elements return as valid to prevent chaining of multiple errors (other validators should handle this)
    validateNonEmptyDateOptions(day, month, year) match {
      case  false => true
      case _ => dateIsFuture(day.get, month.get, year.get)
    }
  }

  def dateMinusMonths(date: Option[Date], months: Int): String = {
    date match {
      case Some(date) =>
        val cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.MONTH, months * -1)
        new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime)
      case _ => ""
    }
  }

  def dateMinusYears(date: Option[Date], years: Int): String = {
    date match {
      case Some(date) =>
        val cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.YEAR, years * -1)
        new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime)
      case _ => ""
    }
  }

  def dateAddMonths(date: Option[Date], months: Int): String = {
    date match {
      case Some(date) =>
        val cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.MONTH, months)
        new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime)
      case _ => ""
    }
  }

  def dateAddYears(date: Option[Date], years: Int): String = {
    date match {
      case Some(date) =>
        val cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.YEAR, years)
        new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime)
      case _ => ""
    }
  }
}
