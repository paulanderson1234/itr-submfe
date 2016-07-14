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

package forms

import forms.TenYearPlanForm._
import models.TenYearPlanModel
import uk.gov.hmrc.play.test.UnitSpec


class TenYearPlanFormSpec extends UnitSpec {

  "Creating the form for the date of incorporation date" should {
    "return a populated yes form using .fill" in {
      val model = TenYearPlanModel("Yes", Some("TEXT"))
      val form = tenYearPlanForm.fill(model)
      form.value.get shouldBe TenYearPlanModel("Yes", Some("TEXT"))
    }

    "return a populated no form using .fill" in {
      val model = TenYearPlanModel("No", None)
      val form = tenYearPlanForm.fill(model)
      form.value.get shouldBe TenYearPlanModel("No", None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasTenYearPlan","Yes"),("tenYearPlanDesc", "TEXT"))
      val form = tenYearPlanForm.bind(map)
      form.value shouldBe Some(TenYearPlanModel("Yes", Some("TEXT")))
    }

    "return a None if a model with both a 'No' selection and date present using .bind" in {
      val map = Map(("hasTenYearPlan","No"),("tenYearPlanDesc", "TEXT"))
      val form = tenYearPlanForm.bind(map)
      form.hasErrors shouldBe true
    }
  }
}
