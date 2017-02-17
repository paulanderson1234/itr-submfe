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

import models.RegisteredAddressModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class RegisteredAddressFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    RegisteredAddressForm.registeredAddressForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    RegisteredAddressForm.registeredAddressForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val RegisteredAddressJson = """{"postcode":"TF13NY"}"""
  val registeredAddressModel = RegisteredAddressModel("TF13NY")

  "The Registered Address Form" should {
    "Not return an error if lower case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "AA1 1AA"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The Registered Address Form" should {
    "Not return an error if in mixed case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "AA1 1AA"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The Registered Address Form" should {
    "Not return an error if mixed case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "AA1 1AA"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The Registered Address Form" should {
    "Not return an error if postcode regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "TF11 2FT"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The Registered Address Form" should {
    "Return an error if postcode regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "TF11 2FTR"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcodelookup")
          err.args shouldBe Array()
        }
        case _ => fail("Missing error")
      }
    }
  }

  "The Registered Address Form" should {
    "Return an error if postcode regex pattern contains multiple spaces" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "TF1  2FT"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcodelookup")
          err.args shouldBe Array()
        }
        case _ => fail("Missing error")
      }
    }
  }

  "The Registered Address Form" should {
    "Return an error if postcode regex pattern contains leading spaces" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> " TF1 2FT"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcodelookup")
          err.args shouldBe Array()
        }
        case _ => fail("Missing error")
      }
    }
  }

  "The Registered Address Form" should {
    "Return an error if postcode regex pattern contains spaces at end" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "TF1 1ET "
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcodelookup")
          err.args shouldBe Array()
        }
        case _ => fail("Missing error")
      }
    }
  }

  "The Registered Address Form" should {
    "Return an error if postcode regex pattern contains invalid characters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "TF£ 1%&"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcodelookup")
          err.args shouldBe Array()
        }
        case _ => fail("Missing error")
      }
    }
  }

  // model from json
  "The Registered Address Form model" should {
    "load the JSON successfully" in {

      implicit val formats = Json.format[RegisteredAddressModel]

      val address = Json.parse(RegisteredAddressJson).as[RegisteredAddressModel]
      address.postcode shouldBe "TF13NY"
    }
  }

  // model to json
  "The Registered Address Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[RegisteredAddressModel]

      val addressJson = Json.toJson(registeredAddressModel).toString()
      addressJson shouldBe RegisteredAddressJson

    }
  }

  // form model to json - apply
  "The Registered Address Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[RegisteredAddressModel]
      val registeredAddressForm =RegisteredAddressForm.registeredAddressForm.fill(registeredAddressModel)
      registeredAddressForm.get.postcode shouldBe "TF13NY"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[RegisteredAddressModel]
      val registeredAddressForm = RegisteredAddressForm.registeredAddressForm.fill(registeredAddressModel)
      val formJson = Json.toJson(registeredAddressForm.get).toString
      formJson shouldBe RegisteredAddressJson
    }
  }
}
