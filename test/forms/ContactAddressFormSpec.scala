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

import models.ContactAddressModel
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class ContactAddressFormSpec extends UnitSpec {

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    ContactAddressForm.contactAddressForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    ContactAddressForm.contactAddressForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val ContactAddressJson = """{"postcode":"TF13NY"}"""
  val contactddressModel = ContactAddressModel("TF13NY")

  "The Contact Address Form" should {
    "Not return an error if lower case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "tf1 3ny"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The Contact Address Form" should {
    "Not return an error if in mixed case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "postcode" -> "tF1 3Ny"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }


  "The Contact Address Form" should {
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

  "The Contact Address Form" should {
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

  "The Contact Address Form" should {
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

  "The Contact Address Form" should {
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

  "The Contact Address Form" should {
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

  "The Contact Address Form" should {
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
  "The Contact Address Form model" should {
    "load the JSON successfully" in {

      implicit val formats = Json.format[ContactAddressModel]

      val address = Json.parse(ContactAddressJson).as[ContactAddressModel]
      address.postcode shouldBe "TF13NY"
    }
  }

  // model to json
  "The Contact Address Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[ContactAddressModel]

      val addressJson = Json.toJson(contactddressModel).toString()
      addressJson shouldBe ContactAddressJson

    }
  }

  // form model to json - apply
  "The Contact Address Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[ContactAddressModel]
      val contactAddressForm =ContactAddressForm.contactAddressForm.fill(contactddressModel)
      contactAddressForm.get.postcode shouldBe "TF13NY"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[ContactAddressModel]
      val contactAddressForm = ContactAddressForm.contactAddressForm.fill(contactddressModel)
      val formJson = Json.toJson(contactAddressForm.get).toString
      formJson shouldBe ContactAddressJson
    }
  }
}
