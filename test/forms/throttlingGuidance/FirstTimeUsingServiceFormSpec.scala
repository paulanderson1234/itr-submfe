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

package forms.throttlingGuidance

import common.Constants

import models.throttlingGuidance.FirstTimeUsingServiceModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class FirstTimeUsingServiceFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    FirstTimeUsingServiceForm.firstTimeUsingServiceForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    FirstTimeUsingServiceForm.firstTimeUsingServiceForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val firstTimeUsingServiceJson = """{"isFirstTimeUsingService":"Yes"}"""
  val firstTimeUsingServiceModel = FirstTimeUsingServiceModel("Yes")


  "The first Time Using Service Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isFirstTimeUsingService" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "isFirstTimeUsingService"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The first Time Using Service Form" should {
    "not return an error if the 'Yes' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isFirstTimeUsingService" -> Constants.StandardRadioButtonYesValue
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }


  "The first Time Using Service Form" should {
    "not return an error if the 'No' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isFirstTimeUsingService" -> Constants.StandardRadioButtonNoValue
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
  "The is agent model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[FirstTimeUsingServiceModel]

      val firstTimeUsingService= Json.toJson(firstTimeUsingServiceModel).toString()
      firstTimeUsingService shouldBe firstTimeUsingServiceJson

    }
  }

  // form model to json - apply
  "The first Time Using Service Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[FirstTimeUsingServiceModel]
      val firstTimeUsingServiceForm = FirstTimeUsingServiceForm.firstTimeUsingServiceForm.fill(firstTimeUsingServiceModel)
      firstTimeUsingServiceForm.get.isFirstTimeUsingService shouldBe Constants.StandardRadioButtonYesValue
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[FirstTimeUsingServiceModel]
      val firstTimeUsingServiceForm = FirstTimeUsingServiceForm.firstTimeUsingServiceForm.fill(firstTimeUsingServiceModel)
      val formJson = Json.toJson(firstTimeUsingServiceForm.get).toString()
      formJson shouldBe firstTimeUsingServiceJson
    }
  }
}
