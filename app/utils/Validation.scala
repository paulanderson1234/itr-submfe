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

import java.text.SimpleDateFormat

import models.{CompanyAddressModel, DateOfFirstSaleModel}
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages
import common.Dates

object Validation {

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


  def isValidDate(day: Int, month: Int, year: Int): Boolean = {
    try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"${day}/${month}/${year}")
      true
    } catch {

      case e: Exception =>{
        false
      }
    }
  }
}