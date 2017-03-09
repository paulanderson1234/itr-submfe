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

import models.{RegisteredAddressModel, PreviousSchemeDeleteModel}
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class PreviousDeleteFormSpec extends UnitSpec with OneAppPerSuite{

  val previousSchemeDeleteJson = """{"previousSchemeId":"1"}"""
  val previousSchemeDeleteModel = PreviousSchemeDeleteModel("1")

  // form model to json - apply
  "The PreviousDeleteForm model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[PreviousSchemeDeleteModel]
      val previousSchemeDeleteForm = PreviousSchemeDeleteForm.previousSchemeDeleteForm.fill(previousSchemeDeleteModel)
      previousSchemeDeleteForm.get.previousSchemeId shouldBe previousSchemeDeleteModel.previousSchemeId
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[PreviousSchemeDeleteModel]
      val previousSchemeDeleteForm = PreviousSchemeDeleteForm.previousSchemeDeleteForm.fill(previousSchemeDeleteModel)
      val formJson = Json.toJson(previousSchemeDeleteForm.get).toString()
      formJson shouldBe previousSchemeDeleteJson
    }
  }
}
