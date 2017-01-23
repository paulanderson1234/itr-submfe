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
import models.{AddressModel, ConfirmCorrespondAddressModel}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class ConfirmCorrespondAddressFormSpec extends UnitSpec with OneAppPerSuite{

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

  val address = AddressModel("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AA1 1AA"), "GB")
  val confirmCorrespondAddressJson = """{"contactAddressUse":"Yes","address":{"addressline1":"Line 1","addressline2":"Line 2","addressline3":"Line 3","addressline4":"Line 4","postcode":"AA1 1AA","countryCode":"GB"}}"""
  val confirmCorrespondAddressModel = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue, address)

  "The Confirm Correspondence Address Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "contactAddressUse" -> "",
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "AA1 1AA",
        "address.countryCode" -> "GB"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "contactAddressUse"
          Messages(err.message) shouldBe Messages("error.required")
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
      "contactAddressUse" -> Constants.StandardRadioButtonYesValue,
      "address.addressline1" -> "Line 1",
      "address.addressline2" -> "Line 2",
      "address.addressline3" -> "Line 3",
      "address.addressline4" -> "line 4",
      "address.postcode" -> "AA1 1AA",
      "address.countryCode" -> "GB"
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
