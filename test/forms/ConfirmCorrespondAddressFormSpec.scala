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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package forms

import common.Constants
import models.ConfirmCorrespondAddressModel
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class ConfirmCorrespondAddressFormSpec extends UnitSpec {

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors.head),
      userData => None
    )
  }

  val confirmCorrespondAddressJson = """{"contactAddressUse":"Yes"}"""
  val confirmCorrespondAddressModel = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue)

  "The Confirm Correspondence Address Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "contactAddressUse" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "contactAddressUse"
          err.message shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Confirm Correspondence Address Form" should {
    "not return an error if the 'Yes' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "contactAddressUse" -> Constants.StandardRadioButtonYesValue
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }


  // form model to json - apply
  "The Confirm Correspondence Address Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[ConfirmCorrespondAddressModel]
      val confirmCorrespondAddressForm = ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.fill(confirmCorrespondAddressModel)
      confirmCorrespondAddressForm.get.contactAddressUse shouldBe Constants.StandardRadioButtonYesValue
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[ConfirmCorrespondAddressModel]
      val confirmCorrespondAddressForm = ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.fill(confirmCorrespondAddressModel)
      val formJson = Json.toJson(confirmCorrespondAddressForm.get).toString()
      formJson shouldBe confirmCorrespondAddressJson
    }
  }
}
