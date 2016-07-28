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

import models.{IsKnowledgeIntensiveModel, WhatWillUseForModel}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class WhatWillUseForFormSpec extends UnitSpec {

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    WhatWillUseForForm.whatWillUseForForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    WhatWillUseForForm.whatWillUseForForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val whatWillUseForJson = """{"whatWillUseFor":"Doing business"}"""
  val whatWillUseForModel = WhatWillUseForModel("Doing business")

  "The What Will Use For Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "whatWillUseFor" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "whatWillUseFor"
          err.message shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Is Knowledge Intensive Form" should {
    "not return an error if the 'Doing business' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "whatWillUseFor" -> "Doing business"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }


  "The What Will Use For Form" should {
    "not return an error if the 'Getting ready for business' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "whatWillUseFor" -> "Getting ready for business"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  "The What Will Use For Form" should {
    "not return an error if the 'Research and Development' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development"
      )
      bindWithError(request) match {
        case Some(err) => fail("Validation error not expected")
        case _ => ()
      }
    }
  }

  // model to json
  "The What Will Use For Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[WhatWillUseForModel]

      val whatWillUseFor = Json.toJson(whatWillUseForModel).toString()
      whatWillUseFor shouldBe whatWillUseForJson

    }
  }

  // form model to json - apply
  "The What Will Use For Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[WhatWillUseForModel]
      val whatWillUseForForm = WhatWillUseForForm.whatWillUseForForm.fill(whatWillUseForModel)
      whatWillUseForForm.get.whatWillUseFor shouldBe "Doing business"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[WhatWillUseForModel]
      val whatWillUseForForm = WhatWillUseForForm.whatWillUseForForm.fill(whatWillUseForModel)
      val formJson = Json.toJson(whatWillUseForForm.get).toString
      formJson shouldBe whatWillUseForJson
    }
  }
}
