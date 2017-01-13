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

package Forms

import forms.TaxPayerReferenceForm
import models.{ContactDetailsModel, SubmissionRequest, TaxpayerReferenceModel, YourCompanyNeedModel}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class TaxPayerReferenceFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    TaxPayerReferenceForm.taxPayerReferenceForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    TaxPayerReferenceForm.taxPayerReferenceForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val taxpayerReferenceJson = """{"utr":"1111111111"}"""
  val taxpayerReferenceModel = TaxpayerReferenceModel("1111111111")

  // address line 1 validation
  "The utr tax reference Form" should {
    "return an error if utr is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "not return an error if regex pattern is at the borderline condition (10 numbers)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "1234567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr regex pattern is above borderline condition (11 numbers)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "12345678901"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr regex pattern is below borderline condition (9 numbers)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "123456789"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr is a ten digit negative number)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "-1234567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr is a ten digits including a negative sign)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "-123456789"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }


  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(1st char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "a234567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(2nd char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "1b34567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(3rd char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "12c4567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(4th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "123d567890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(5th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "1234e67890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(6th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "12345f7890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(7th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "123456g890"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(8th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "1234567h90"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(9th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "12345678i0"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The utr tax reference Form" should {
    "return an error if utr has correct length with non numeric character(10th char)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "utr" -> "123456789j"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "utr"
          err.message shouldBe Messages("validation.error.utrTenChar")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // model to json
  "The utr Form model" should {
    "load convert to JSON successfully" in {
      implicit val formats = Json.format[TaxpayerReferenceModel]
      val utrJson = Json.toJson(taxpayerReferenceModel).toString()
      utrJson shouldBe taxpayerReferenceJson
    }
  }

  // form model to json - apply
  "The utr Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[TaxpayerReferenceModel]
      val taxPayerReferenceForm = TaxPayerReferenceForm.taxPayerReferenceForm.fill(taxpayerReferenceModel)
      taxPayerReferenceForm.get.utr shouldBe "1111111111"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[TaxpayerReferenceModel]
      val taxPayerReferenceForm = TaxPayerReferenceForm.taxPayerReferenceForm.fill(taxpayerReferenceModel)
      val formJson = Json.toJson(taxPayerReferenceForm.get).toString()
      formJson shouldBe taxpayerReferenceJson
    }

    // form json to model - unapply
    "call unapply successfully to create ss Json" in {
      implicit val formats = Json.format[SubmissionRequest]
      val cd = ContactDetailsModel("gary", "hull", Some("01952 256555"), None, "fred@fred.com")
      val yd = YourCompanyNeedModel("AA")
      val sub = new SubmissionRequest(cd,yd)
    }
  }
}
