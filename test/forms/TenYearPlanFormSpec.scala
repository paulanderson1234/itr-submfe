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

import common.Constants
import forms.TenYearPlanForm._
import models.TenYearPlanModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class TenYearPlanFormSpec extends UnitSpec with OneAppPerSuite{

  "Creating the form for the Ten Year Plan" should {
    "return a populated yes form using .fill" in {
      val model = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("TEXT"))
      val form = tenYearPlanForm.fill(model)
      form.value.get shouldBe TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("TEXT"))
    }

    "return a populated no form using .fill" in {
      val model = TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)
      val form = tenYearPlanForm.fill(model)
      form.value.get shouldBe TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasTenYearPlan", Constants.StandardRadioButtonYesValue), ("tenYearPlanDesc", "TEXT"))
      val form = tenYearPlanForm.bind(map)
      form.value shouldBe Some(TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("TEXT")))
    }

    "return a Some if a model with both a 'No' selection and data present using .bind." +
      "Validation should allow both No and some text to be entered (text is cleared on submission in controller)" in {
      val map = Map(("hasTenYearPlan", Constants.StandardRadioButtonNoValue), ("tenYearPlanDesc", "TEXT"))
      val form = tenYearPlanForm.bind(map)
      form.value shouldBe Some(TenYearPlanModel(Constants.StandardRadioButtonNoValue, Some("TEXT")))
      form.hasErrors shouldBe false
    }

    "when no input is selected the form" should {
      lazy val form = tenYearPlanForm.bind(Map(("hasTenYearPlan", ""), ("tenYearPlanDesc", "")))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "hasTenYearPlan"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("hasTenYearPlan").get.message) shouldBe Messages("error.required")
      }
    }

    "when Yes input is selected with no text description the form" should {
      lazy val form = tenYearPlanForm.bind(Map(("hasTenYearPlan", Constants.StandardRadioButtonYesValue), ("tenYearPlanDesc", "")))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1

      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe Messages("validation.common.error.fieldRequired")
      }
    }
  }
}
