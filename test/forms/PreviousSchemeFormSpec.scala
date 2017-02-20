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

package forms

import java.time.ZoneId
import java.util.Date
import models.PreviousSchemeModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.PreviousSchemeForm._
import common.Constants
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class PreviousSchemeFormSpec extends UnitSpec with OneAppPerSuite{

  val maxAllowableAmount: Int = 999999999
  val minAllowableAmount: Int = 1
  val maxExceeded: String = (maxAllowableAmount + 1).toString
  val belowMinimum: String = (minAllowableAmount - 1).toString
  val otherSchemeNameMax: String = "This is exactly 50 chars long so should be allowed"
  val otherSchemeNameOverMax: String = "This is exactly 51 chars long so should be allowed."

  val date = new Date();
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating a form using an empty model" should {
    lazy val form = previousSchemeForm
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model for basic model" in {
      val model = PreviousSchemeModel(
        Constants.schemeTypeEis, 5000, None, None, Some(21), Some(4), Some(1980), Some(1))

      val form = previousSchemeForm.fill(model)
      form.data("schemeTypeDesc") shouldBe Constants.schemeTypeEis
      form.data("investmentAmount") shouldBe "5000"
      //form.data("investmentSpent") shouldBe None
      //form.data("otherSchemeName") shouldBe None
      form.data("investmentDay") shouldBe "21"
      form.data("investmentMonth") shouldBe "4"
      form.data("investmentYear") shouldBe "1980"
      form.data("processingId") shouldBe "1"

      form.errors.length shouldBe 0
    }

    "return a form with the valid data specified in the model for Seed Enterprise investment scheme" in {
      val model = PreviousSchemeModel(
        Constants.schemeTypeEis, 5000, Some(4500), None, Some(21), Some(4), Some(1980), Some(1))

      val form = previousSchemeForm.fill(model)
      form.data("schemeTypeDesc") shouldBe Constants.schemeTypeEis
      form.data("investmentAmount") shouldBe "5000"
      form.data("investmentSpent") shouldBe "4500"
      form.data("investmentDay") shouldBe "21"
      form.data("investmentMonth") shouldBe "4"
      form.data("investmentYear") shouldBe "1980"
      form.data("processingId") shouldBe "1"
      form.errors.length shouldBe 0
    }

    "return a form with the valid data specified in the model for Another investment scheme" in {
      val model = PreviousSchemeModel(
        Constants.schemeTypeOther, 5000, None, Some("test Scheme"), Some(21), Some(4), Some(1980), Some(1))

      val form = previousSchemeForm.fill(model)
      form.data("schemeTypeDesc") shouldBe Constants.schemeTypeOther
      form.data("investmentAmount") shouldBe "5000"
      form.data("otherSchemeName") shouldBe "test Scheme"
      form.data("investmentDay") shouldBe "21"
      form.data("investmentMonth") shouldBe "4"
      form.data("investmentYear") shouldBe "1980"
      form.data("processingId") shouldBe "1"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form with valid post" when {
    "supplied with minimum required option data" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> "3",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "4",
        "investmentMonth" -> "5",
        "investmentYear" -> "2008",
        "processingId" -> "1"
      ))
      "not raise form error" in {
        form.hasErrors shouldBe false
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for investment amount" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> "",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "4",
        "investmentMonth" -> "5",
        "investmentYear" -> "2008",
        "processingId" -> "1"
      ))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentAmount"
      }
      "associate the correct error message to the error" in {
        form.error("investmentAmount").get.message shouldBe Messages("validation.common.error.fieldRequired")
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with non numeric data for investment amount" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "fred",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentAmount"
        }
        "associate the correct error message to the error" in {
          form.error("investmentAmount").get.message shouldBe Messages("page.investment.amount.invalidAmount")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with negative investment amount" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "-1",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentAmount"
        }
        "associate the correct error message to the error" in {
          form.error("investmentAmount").get.message shouldBe Messages("page.investment.PreviousScheme.investmentAmount.OutOfRange")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with investment amount above maximum allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> maxExceeded,
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentAmount"
        }
        "associate the correct error message to the error" in {
          form.error("investmentAmount").get.message shouldBe Messages("page.investment.PreviousScheme.investmentAmount.OutOfRange")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with investment amount below minimum allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> belowMinimum,
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentAmount"
        }
        "associate the correct error message to the error" in {
          form.error("investmentAmount").get.message shouldBe Messages("page.investment.PreviousScheme.investmentAmount.OutOfRange")
        }
      }
    }

    "supplied with empty space for amount" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> " ",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "3",
        "investmentMonth" -> "5",
        "investmentYear" -> "2006",
        "processingId" -> "1"
      ))
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentAmount"
      }
      "associate the correct error message to the error" in {
        form.error("investmentAmount").get.message shouldBe Messages("validation.common.error.fieldRequired")
      }
    }

    "supplied an amount with decimals" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> "10.00",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "3",
        "investmentMonth" -> "5",
        "investmentYear" -> "2006",
        "processingId" -> "1"
      ))
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentAmount"
      }
      "associate the correct error message to the error" in {
        form.error("investmentAmount").get.message shouldBe Messages("page.investment.amount.invalidAmount")
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for scheme type option" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> "",
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "schemeTypeDesc"
        }
        "associate the correct error message to the error" in {
          Messages(form.error("schemeTypeDesc").get.message) shouldBe Messages("error.required")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for share date day" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("validation.error.DateNotEntered")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for share date month" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("validation.error.DateNotEntered")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for share date year" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("validation.error.DateNotEntered")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with non numeric data for share date day" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "fred",
          "investmentMonth" -> "5",
          "investmentYear" -> "2005",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentDay"
        }
        "associate the correct error message to the error" in {
          Messages(form.error("investmentDay").get.message) shouldBe Messages("error.number")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with non numeric data for share date month" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "jack",
          "investmentYear" -> "2005",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentMonth"
        }
        "associate the correct error message to the error" in {
          Messages(form.error("investmentMonth").get.message) shouldBe Messages("error.number")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with non numeric data for share date year" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "bob",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentYear"
        }
        "associate the correct error message to the error" in {
          Messages(form.error("investmentYear").get.message) shouldBe Messages("error.number")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with invalid date 29 the February in non-leap year" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "29",
          "investmentMonth" -> "2",
          "investmentYear" -> "2001",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("common.date.error.invalidDate")
        }
      }
    }

    "Creating a form with valid post" when {
      "supplied with valid investment share date of 29th February in a leap year" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "3",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "29",
          "investmentMonth" -> "2",
          "investmentYear" -> "2000",
          "processingId" -> "1"
        ))
        "not raise form error" in {
          form.hasErrors shouldBe false
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with invalid date 31 June" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "31",
          "investmentMonth" -> "6",
          "investmentYear" -> "2001",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("common.date.error.invalidDate")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with date in the future for share date year" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeEis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> tomorrowDay,
          "investmentMonth" -> tomorrowMonth,
          "investmentYear" -> tomorrowYear,
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.globalErrors.size shouldBe 1
        }
        "associate the correct error message to the error" in {
          form.globalErrors.head.message shouldBe Messages("validation.error.ShareIssueDate.Future")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for investment spent when scheme type is SEIS" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeSeis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentSpent"
        }
        "associate the correct error message to the error" in {
          form.error("investmentSpent").get.message shouldBe Messages("validation.common.error.fieldRequired")
        }
      }
    }

    "Creating a form using a valid post" when {
      "supplied with both data for investment spent and scheme type is SEIS" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeSeis,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "50",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "not raise a form error" in {
          form.hasErrors shouldBe false
        }

      }
    }

    "Creating a form using an invalid post" when {
      "supplied with investment spent above maximum allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeSeis,
          "investmentAmount" -> "200",
          "investmentSpent" -> maxExceeded,
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentSpent"
        }
        "associate the correct error message to the error" in {
          form.error("investmentSpent").get.message shouldBe Messages("page.investment.PreviousScheme.investmentAmount.OutOfRange")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with investment spent below minimum allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeSeis,
          "investmentAmount" -> "500",
          "investmentSpent" -> belowMinimum,
          "otherSchemeName" -> "",
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "investmentSpent"
        }
        "associate the correct error message to the error" in {
          form.error("investmentSpent").get.message shouldBe Messages("page.investment.PreviousScheme.investmentAmount.OutOfRange")
        }
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with other scheme name above maximun allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeOther,
          "investmentAmount" -> "500",
          "investmentSpent" -> "",
          "otherSchemeName" -> otherSchemeNameOverMax,
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "otherSchemeName"
        }
        "associate the correct error message to the error" in {
          form.error("otherSchemeName").get.message shouldBe Messages("page.investment.PreviousScheme.otherScheme.OutOfRange")
        }
      }
    }

    "Creating a form using an valid post" when {
      "supplied with other scheme name exactly at maximun allowed" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeOther,
          "investmentAmount" -> "500",
          "investmentSpent" -> "",
          "otherSchemeName" -> otherSchemeNameMax,
          "investmentDay" -> "3",
          "investmentMonth" -> "5",
          "investmentYear" -> "2006",
          "processingId" -> "1"
        ))
        "not raise form error" in {
          form.hasErrors shouldBe false
        }
      }
    }

    "supplied with empty space for amount spent" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeSeis,
        "investmentAmount" -> "25",
        "investmentSpent" -> " ",
        "otherSchemeName" -> "",
        "investmentDay" -> "3",
        "investmentMonth" -> "5",
        "investmentYear" -> "2006",
        "processingId" -> "1"
      ))
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentSpent"
      }
      "associate the correct error message to the error" in {
        form.error("investmentSpent").get.message shouldBe Messages("validation.common.error.fieldRequired")
      }
    }

    "supplied an amount spent with decimals" should {
      lazy val form = previousSchemeForm.bind(Map(
        "schemeTypeDesc" -> Constants.schemeTypeSeis,
        "investmentAmount" -> "10",
        "investmentSpent" -> "20.00",
        "otherSchemeName" -> "",
        "investmentDay" -> "3",
        "investmentMonth" -> "5",
        "investmentYear" -> "2006",
        "processingId" -> "1"
      ))
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentSpent"
      }
      "associate the correct error message to the error" in {
        form.error("investmentSpent").get.message shouldBe Messages("page.investment.amount.invalidAmount")
      }
    }

    "Creating a form using an invalid post" when {
      "supplied with no data for other scheme name spent when scheme type is Another " should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeOther,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "raise form error" in {
          form.hasErrors shouldBe true
        }
        "raise 1 form error" in {
          form.errors.length shouldBe 1
          form.errors.head.key shouldBe "otherSchemeName"
        }
        "associate the correct error message to the error" in {
          Messages(form.error("otherSchemeName").get.message) shouldBe Messages("error.required")
        }
      }
    }

    "Creating a form using a valid post" when {
      "supplied with both data for other scheme name and and scheme type is Another" should {
        lazy val form = previousSchemeForm.bind(Map(
          "schemeTypeDesc" -> Constants.schemeTypeOther,
          "investmentAmount" -> "5000",
          "investmentSpent" -> "",
          "otherSchemeName" -> "Scheme name present",
          "investmentDay" -> "4",
          "investmentMonth" -> "5",
          "investmentYear" -> "2008",
          "processingId" -> "1"
        ))
        "not raise a form error" in {
          form.hasErrors shouldBe false
        }

      }
    }

  }
}
