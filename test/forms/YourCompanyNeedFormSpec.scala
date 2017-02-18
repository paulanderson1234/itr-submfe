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

import models.YourCompanyNeedModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._


class YourCompanyNeedFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    YourCompanyNeedForm.yourCompanyNeedForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    YourCompanyNeedForm.yourCompanyNeedForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val companyNeedJson = """{"needAAorCS":"AA"}"""
  val companyNeedModel = YourCompanyNeedModel("AA")

  // address line 1 validation
  "The Your Company Need Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "needAAorCS" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "needAAorCS"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Your Company Need Form" should {
    "not return an error if the AA option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "needAAorCS" -> "AA"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }


  "The Your Company Need Form" should {
    "not return an error if the CS option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "needAAorCS" -> "CS"
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
  "The Company Address Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[YourCompanyNeedModel]

      val needAAorCS = Json.toJson(companyNeedModel).toString()
      needAAorCS shouldBe companyNeedJson

    }
  }

  // form model to json - apply
  "The Your Company Need Form model" should {
    "call apply corrctly on the model" in {
      implicit val formats = Json.format[YourCompanyNeedModel]
      val yourCompanyNeedForm =YourCompanyNeedForm.yourCompanyNeedForm.fill(companyNeedModel)
      yourCompanyNeedForm.get.needAAorCS shouldBe "AA"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[YourCompanyNeedModel]
      val yourCompanyNeedForm = YourCompanyNeedForm.yourCompanyNeedForm.fill(companyNeedModel)
      val formJson = Json.toJson(yourCompanyNeedForm.get).toString()
      formJson shouldBe companyNeedJson
    }
  }
}
