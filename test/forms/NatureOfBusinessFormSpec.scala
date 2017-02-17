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

import forms.NatureOfBusinessForm.natureOfBusinessForm
import models.NatureOfBusinessModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class NatureOfBusinessFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    natureOfBusinessForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    natureOfBusinessForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val natureOfBusinessJson = """{"natureofbusiness":"I sell cars to car warehouse outets in major towns"}"""
  val natureOfBusinessModel = NatureOfBusinessModel("I sell cars to car warehouse outets in major towns")
  
  "The nature of business Form" should {
    "return an error if natureofbusiness is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "natureofbusiness" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "natureofbusiness"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The nature of business Form" should {
    "not return an error if entry at the borderline condition (1 character)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "natureofbusiness" -> "h"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The nature of business Form" should {
    "not return an error if entry is above the suggested 15 word limit (16 words)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "natureofbusiness" -> "this is more than 15 words to see if that amount is suggested but not enforced"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  // model to json
  "The utr Form model" should {
    "load convert to JSON successfully" in {
      implicit val formats = Json.format[NatureOfBusinessModel]
      val utrJson = Json.toJson(natureOfBusinessModel).toString()
      utrJson shouldBe natureOfBusinessJson
    }
  }

  // form model to json - apply
  "The utr Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[NatureOfBusinessModel]
      val form = natureOfBusinessForm.fill(natureOfBusinessModel)
      form.get.natureofbusiness shouldBe "I sell cars to car warehouse outets in major towns"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[NatureOfBusinessModel]
      val form = natureOfBusinessForm.fill(natureOfBusinessModel)
      val formJson = Json.toJson(form.get).toString()
      formJson shouldBe natureOfBusinessJson
    }
  }
}
