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

import models.ProposedInvestmentModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.ProposedInvestmentForm._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class ProposedInvestmentFormSpec extends UnitSpec with OneAppPerSuite{

  "Creating a form using an empty model" should {
    lazy val form = proposedInvestmentForm
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = ProposedInvestmentModel(15)
      val form = proposedInvestmentForm.fill(model)
      form.data("investmentAmount") shouldBe "15"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for amount" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> ""))
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

    "supplied with empty space for amount" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "  "))
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

    "supplied with non numeric input for amount" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "a"))
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

    "supplied an amount with decimals" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "10.00"))
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

    "supplied with an amount that's greater than the max" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "5000001"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentAmount"
      }
      "associate the correct error message to the error" in {
        form.error("investmentAmount").get.message shouldBe Messages("page.investment.amount.OutOfRange")
      }
    }

    "supplied with an amount that's lower than the min" should {
      lazy val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "0"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "investmentAmount"
      }
      "associate the correct error message to the error" in {
        form.error("investmentAmount").get.message shouldBe Messages("page.investment.amount.OutOfRange")
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount at the maximum allowed" should {
      "not raise form error" in {
        val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "5000000"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with valid amount at the minimum allowed" should {
      "not raise form error" in {
        val form = proposedInvestmentForm.bind(Map("investmentAmount" -> "1"))
        form.hasErrors shouldBe false
      }
    }

  }
}
