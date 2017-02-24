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

import java.text.SimpleDateFormat

import models._
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation._
import play.api.i18n.Messages
import java.util.{Calendar, Date}

import common.Constants
import models.submission.SchemeTypesModel
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object Validation {

  val EmailThresholdLength = 132

  // use new Date() to get the date now
  lazy val sf = new SimpleDateFormat("dd/MM/yyyy")
  lazy val datePageFormat = new SimpleDateFormat("dd MMMM yyyy")
  lazy val datePageFormatNoZero = new SimpleDateFormat("d MMMM yyyy")
  lazy val desReverseDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def previousSchemeValidation: Constraint[PreviousSchemeModel] = {

    Constraint("constraints.previous_investment")({
      investmentForm: PreviousSchemeModel => {
        anyEmpty(investmentForm.day, investmentForm.month, investmentForm.year) match {
          case true => Invalid(Seq(ValidationError(Messages("validation.error.DateNotEntered"))))
          case false => isValidDate(investmentForm.day.get, investmentForm.month.get, investmentForm.year.get) match {
            case false => Invalid(Seq(ValidationError(Messages("common.date.error.invalidDate"))))
            case true => dateNotInFuture(investmentForm.day.get, investmentForm.month.get, investmentForm.year.get) match {
              case true => Valid
              case false => Invalid(Seq(ValidationError(Messages("validation.error.ShareIssueDate.Future"))))
            }
          }
        }
      }
    })
  }

  def dateOfCommercialSaleDateValidation: Constraint[CommercialSaleModel] = {

    def validateYes(dateForm: CommercialSaleModel) = {
      anyEmpty(dateForm.commercialSaleDay, dateForm.commercialSaleMonth, dateForm.commercialSaleYear) match {
        case true => Invalid(Seq(ValidationError(Messages("validation.error.DateNotEntered"))))
        case false => isValidDate(dateForm.commercialSaleDay.get, dateForm.commercialSaleMonth.get, dateForm.commercialSaleYear.get) match {
          case false => Invalid(Seq(ValidationError(Messages("common.date.error.invalidDate"))))
          case true => dateNotInFuture(dateForm.commercialSaleDay.get, dateForm.commercialSaleMonth.get, dateForm.commercialSaleYear.get) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.ShareIssueDate.Future"))))
          }
        }
      }
    }

    Constraint("constraints.date_of_first_sale")({
      dateForm: CommercialSaleModel =>
        dateForm.hasCommercialSale match {
          case Constants.StandardRadioButtonNoValue => allDatesEmpty(dateForm.commercialSaleDay,
            dateForm.commercialSaleMonth, dateForm.commercialSaleYear) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.DateForNoOption"))))
          }
          case Constants.StandardRadioButtonYesValue => validateYes(dateForm)
        }
    })
  }

  def tradeStartDateValidation: Constraint[TradeStartDateModel] = {

    def validateYes(dateForm: TradeStartDateModel) = {
      anyEmpty(dateForm.tradeStartDay, dateForm.tradeStartMonth, dateForm.tradeStartYear) match {
        case true => Invalid(Seq(ValidationError(Messages("validation.error.DateNotEntered"))))
        case false => isValidDate(dateForm.tradeStartDay.get, dateForm.tradeStartMonth.get, dateForm.tradeStartYear.get) match {
          case false => Invalid(Seq(ValidationError(Messages("common.date.error.invalidDate"))))
          case true => dateNotInFuture(dateForm.tradeStartDay.get, dateForm.tradeStartMonth.get, dateForm.tradeStartYear.get) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.ShareIssueDate.Future"))))
          }
        }
      }
    }

    Constraint("constraints.trade_start_date")({
      dateForm: TradeStartDateModel =>
        dateForm.hasTradeStartDate match {
          case Constants.StandardRadioButtonNoValue => allDatesEmpty(dateForm.tradeStartDay,
            dateForm.tradeStartMonth, dateForm.tradeStartYear) match {
            case true => Valid
            case false => Invalid(Seq(ValidationError(Messages("validation.error.DateForNoOption"))))
          }
          case Constants.StandardRadioButtonYesValue => validateYes(dateForm)
        }
    })
  }


  def tenYearPlanDescValidation: Constraint[TenYearPlanModel] = {

    def validateFields(hasPlan: String, planDesc: Option[String]): Boolean = {
      if (hasPlan.isEmpty || planDesc.isEmpty || planDesc.get.length > Constants.SuggestedTextMaxLengthLower) {
        true
      } else
        false
    }

    def validateYes(tenYearForm: TenYearPlanModel) = {
      validateFields(tenYearForm.hasTenYearPlan, tenYearForm.tenYearPlanDesc) match {
        case true => Invalid(Seq(ValidationError(Messages("validation.common.error.fieldRequired"))))
        case false => Valid
      }
    }

    Constraint("constraints.ten_year_plan")({
      tenYearForm: TenYearPlanModel =>
        tenYearForm.hasTenYearPlan match {
          case Constants.StandardRadioButtonNoValue => if (tenYearForm.hasTenYearPlan.isEmpty)
            Invalid(Seq(ValidationError(Messages("validation.common.error.fieldRequired"))))
          else Valid
          case Constants.StandardRadioButtonYesValue => validateYes(tenYearForm)
        }
    })
  }

  def anyEmpty(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    if (day.isEmpty || month.isEmpty || year.isEmpty) {
      true
    } else {
      false
    }
  }

  def allDatesEmpty(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    if (day.isEmpty & month.isEmpty & year.isEmpty) {
      true
    } else {
      false
    }
  }

  def validateNonEmptyDateOptions(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    if (day.isEmpty || month.isEmpty || year.isEmpty) {
      false
    } else {
      true
    }
  }

  def mandatoryAddressLineCheck: Mapping[String] = {
    val validAddressLine = """[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,34}""".r
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

  def mandatoryMaxTenNumberValidation(message: String): Mapping[String] = {
    val validNum = """[0-9]{1,9}""".r
    val numCharCheckConstraint: Constraint[String] =
      Constraint("contraints.mandatoryNumberCheck")({
        text =>
          val error = text match {
            case validNum() => Nil
            case _ => Seq(ValidationError(Messages(message)))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text.verifying(numCharCheckConstraint)
  }

  def mandatoryMaxTenNumberNonZeroValidation(message: String): Mapping[String] = {
    val validNum = """^[1-9][0-9]{0,8}$""".r
    val numCharCheckConstraint: Constraint[String] =
      Constraint("contraints.mandatoryNumberCheck")({
        text =>
          val error = text match {
            case validNum() => Nil
            case _ => Seq(ValidationError(Messages(message)))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text.verifying(numCharCheckConstraint)
  }

  def optionalAddressLineCheck: Mapping[String] = {
    val validAddressLine = """^$|[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,34}""".r
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
    val validAddressLine = """^$|[a-zA-Z0-9,.\(\)/&'"\-]{1}[a-zA-Z0-9, .\(\)/&'"\-]{0,34}""".r
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
    val validPostcodeLine = "^[A-Z]{1,2}[0-9][0-9A-Z]? [0-9][A-Z]{2}$".r
    val postcodeCheckConstraint: Constraint[String] =
      Constraint("constraints.postcode")({
        text =>
          val error = text.toUpperCase match {
            case validPostcodeLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.postcode")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(postcodeCheckConstraint)
  }
  def countryCodeCheck: Mapping[String] = {
    val countryCode = """[A-Z]{2}""".r
    val countryCodeCheckConstraint: Constraint[String] =
      Constraint("constraints.countryCode")({
        text =>
          val error = text match {
            case countryCode() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.countryCode")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(countryCodeCheckConstraint)
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

  def emailCheck(maxLength:Option[Int] = Some(EmailThresholdLength)): Mapping[String] = {
    val validEmailLine = """^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$""".r
    val emailCheckConstraint: Constraint[String] =
      Constraint("constraints.email")({
        text =>
          val error = text match {
            case validEmailLine() if text.length <= EmailThresholdLength => Nil
            case _ => Seq(ValidationError(Messages("validation.error.email")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(emailCheckConstraint)
  }

  def telephoneNumberCheck: Mapping[String] = {
    val validTelephoneNumberLine = """^[A-Z0-9 )/(*#-]{1,24}$""".r
    val telephoneNumberCheckConstraint: Constraint[String] =
      Constraint("contraints.telephoneNumber")({
        text =>
          val error = text match {
            case validTelephoneNumberLine() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.telephoneNumber")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(telephoneNumberCheckConstraint)
  }

  def postcodeCountryCheckConstraint: Constraint[AddressModel] = {
    Constraint("constraints.postcodeCountryCheck")({
      addressForm: AddressModel =>
        if (addressForm.countryCode == "GB" && addressForm.postcode.fold(true)( _.isEmpty)) {
          Invalid(Seq(ValidationError(Messages("validation.error.countrypostcode"))))
        } else {
          Valid
        }
    })
  }

  def schemeTypesConstraint: Constraint[SchemeTypesModel] = {
    Constraint("constraints.schemeSelection")({
      schemeTypeForm =>
        if (schemeTypeForm.equals(SchemeTypesModel(false,false,false,false))) {
          Invalid(Seq(ValidationError(Messages("validation.error.schemeSelection"))))
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

  val integerCheck: String => Boolean = (input) => {
    Try(input.trim.toInt) match {
      case Success(_) => true
      case Failure(_) if input.trim == "" => true
      case Failure(_) => false
    }
  }

  val mandatoryCheck: String => Boolean = (input) => input.trim != ""

  val decimalPlacesCheck: BigDecimal => Boolean = (input) => input.scale < 3

  val decimalPlacesCheckNoDecimal: BigDecimal => Boolean = (input) => input.scale < 1

  def maxIntCheck(maxInteger: Int): Int => Boolean = (input) => input <= maxInteger

  def minIntCheck(minInteger: Int): Int => Boolean = (input) => input >= minInteger

  val yesNoCheck: String => Boolean = {
    case Constants.StandardRadioButtonYesValue => true
    case Constants.StandardRadioButtonNoValue => true
    case "" => true
    case _ => false
  }

  def isValidDateOptions(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    validateNonEmptyDateOptions(day, month, year) match {
      case false => true
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

  def constructDate(day: Int, month: Int, year: Int): Date = {
    sf.parse(s"$day/$month/$year")
  }

  def dateInFuture(date: Date): Boolean = {
    date.after(DateTime.now.toDate)
  }

  /** Determines whether the date of incorporation passed is less than 3 years from today */
  def dateAfterIncorporationRule(day: Int, month: Int, year: Int): Boolean = {
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

  /** Determines whether the first commercial sale was within the age range (10 or 7 years) */
  def checkAgeRule(day: Int, month: Int, year: Int, ageRange: Int): Boolean = {
    Try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"$day/$month/$year")
      constructDate(day, month, year).before(fmt.parse(dateMinusYears(Some(new Date()), ageRange)))
    } match {
      case Success(result) => result
      case Failure(_) => false
    }
  }

  def isSameDate(dateFirst: Date, dateSecond:Date): Boolean = {
    Try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.format(dateFirst).equals(fmt.format(dateSecond))

    } match {
      case Success(result) => result
      case Failure(_) => false
    }
  }

  def isNotSameDate(dateFirst: Date, dateSecond:Date): Boolean = {
    Try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      !fmt.format(dateFirst).equals(fmt.format(dateSecond))

    } match {
      case Success(result) => result
      case Failure(_) => false
    }
  }

  def dateSinceOtherDate(day: Int, month: Int, year: Int, otherDate:Date): Boolean = {
    constructDate(day, month, year).compareTo(otherDate) >= 0
  }

  def dateNotInFuture(day: Int, month: Int, year: Int): Boolean = {
    !constructDate(day, month, year).after(DateTime.now.toDate)
  }

  def dateIsFuture(day: Int, month: Int, year: Int): Boolean = {
    constructDate(day, month, year).after(DateTime.now.toDate)
  }

  def dateNotInFutureOptions(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    // if empty elements return as valid to prevent chaining of multiple errors (other validators should handle this)
    validateNonEmptyDateOptions(day, month, year) match {
      case false => true
      case _ => dateNotInFuture(day.get, month.get, year.get)
    }
  }

  def dateInFutureOptions(day: Option[Int], month: Option[Int], year: Option[Int]): Boolean = {
    // if empty elements return as valid to prevent chaining of multiple errors (other validators should handle this)
    validateNonEmptyDateOptions(day, month, year) match {
      case false => true
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

  def dateToDesFormat(day: Int, month: Int, year: Int): String = {
    Try {
      desReverseDateFormat.format(sf.parse(s"$day/$month/$year"))
    } match {
      case Success(result) => result
      case Failure(_) => {
        ""
      }
    }
  }

  def fourDigitYearCheck: Mapping[String] = {
    val validYear = """[0-9]{4}""".r
    val yearCheckConstraint: Constraint[String] =
      Constraint("contraints.utrTen")({
        text =>
          val error = text match {
            case validYear() => Nil
            case _ => Seq(ValidationError(Messages("validation.error.fourDigitYear")))
          }
          if (error.isEmpty) Valid else Invalid(error)
      })
    text().verifying(yearCheckConstraint)
  }
}
