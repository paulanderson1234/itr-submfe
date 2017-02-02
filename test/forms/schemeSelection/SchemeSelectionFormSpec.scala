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

package forms.schemeSelection

import controllers.helpers.BaseSpec
import models.submission.SchemeTypesModel
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.i18n.Messages.Implicits._

class SchemeSelectionFormSpec extends BaseSpec {

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[Seq[FormError]] = {
    SchemeSelectionForm.schemeSelectionForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors),
      userData => None
    )
  }

  val schemeTypesModel = SchemeTypesModel(eis = true, seis = true, vct = false)

  // address line 1 validation
  "SchemeSelectionForm" should {

    "Return an error if an empty form is submitted" in {
      val request = fakeRequest.withFormUrlEncodedBody(
        "EIS" -> "",
        "SEIS" -> "",
        "VCT" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.size shouldBe 3
          err(0).message shouldBe Messages("error.boolean")
          err(1).message shouldBe Messages("error.boolean")
          err(2).message shouldBe Messages("error.boolean")
        }
        case _ => {
          fail("Missing error")
        }
      }
    }

    "not return an error when a valid form is submitted" in {
      val request = fakeRequest.withFormUrlEncodedBody(
        "EIS" -> "true",
        "SEIS" -> "true",
        "VCT" -> "false"
      )
      val result = SchemeSelectionForm.schemeSelectionForm.bindFromRequest()(request)
      result.value.get shouldEqual schemeTypesModel
    }

  }

}
