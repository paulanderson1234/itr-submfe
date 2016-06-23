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

package Forms

import forms.CompanyAddressForm
import models.CompanyAddressModel
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class CompanyAddressFormSpec extends UnitSpec {

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    CompanyAddressForm.companyAddressForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    CompanyAddressForm.companyAddressForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val companyAddressJson = """{"addressline1":"line 1","addressline2":"line 2","addressline3":"line 3","addressline4":"line 4","postcode":"TF13NY","country":"UK"}"""
  val companyAdressModel = CompanyAddressModel("line 1", "line 2", "line 3", "line 4", "TF13NY", "UK")

  // address line 1 validation
  "The Company Address Form" should {
    "Return an error if address line 1 is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "",
        "postcode" -> "TF4 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline1"
          err.message shouldBe Messages("validation.error.mandatoryaddresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Company Address Form" should {
    "not return an error if address line 1 regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "1234567890 1234567890 12345",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Shropshire",
        "postcode" -> "",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if address line 1 regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "1234567890 1234567890 123456",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Shropshire",
        "postcode" -> "TF4 2FT",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline1"
          err.message shouldBe Messages("validation.error.mandatoryaddresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // address line 2 validation
  "The Company Address Form" should {
    "Return an error if address line 2 is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 The Street",
        "addressline2" -> "",
        "addressline3" -> "Telford",
        "addressline4" -> "",
        "postcode" -> "TF4 2FT",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline2"
          err.message shouldBe Messages("validation.error.mandatoryaddresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Company Address Form" should {
    "not return an error if address line 2 regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "1234567890 1234567890 12345",
        "addressline3" -> "Telford",
        "addressline4" -> "Shropshire",
        "postcode" -> "",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if address line 2 regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "1234567890 1234567890 123456",
        "addressline3" -> "Telford",
        "addressline4" -> "Shropshire",
        "postcode" -> "TF4 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline2"
          err.message shouldBe Messages("validation.error.mandatoryaddresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // address line 3 validation
  "The Company Address Form" should {
    "Not return an error if address line 3 is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "",
        "addressline4" -> "Telford",
        "postcode" -> "",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if address line 3 regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "1234567890 1234567890 12345",
        "addressline4" -> "Telford",
        "postcode" -> "TF4 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if address line 3 regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "134 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "1234567890 1234567890 123456",
        "addressline4" -> "Telford",
        "postcode" -> "TF4 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline3"
          err.message shouldBe Messages("validation.error.optionaladdresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // line 4 validation
  "The Company Address Form" should {
    "Not return an error if address line 4 is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "",
        "postcode" -> "",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if address line 4 regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "1234567890 55 6699",
        "postcode" -> "TF4 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if address line 4 regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "134 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "1234567890 55 66998",
        "postcode" -> "",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addressline4"
          err.message shouldBe Messages("validation.error.linefouraddresssline")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // country validation
  "The Company Address Form" should {
    "Not return an error country is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "",
        "postcode" -> "",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if country regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "",
        "country" -> "some country isvalid"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if country regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "134 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "",
        "country" -> "sone country notvalid"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "country"
          err.message shouldBe Messages("validation.error.country")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // postcode validation
  "The Company Address Form" should {
    "Not return an error if postcode is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if lower case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "tf1 3ny",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if in mixed case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "tF1 3Ny",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if mixed case" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "tF1 3nY",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Not return an error if postcode regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "21 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "TF11 2FT",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Company Address Form" should {
    "Return an error if postcode regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "134 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "TF11 2FTR",
        "country" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "postcode"
          err.message shouldBe Messages("validation.error.postcode")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // cross field validation
  "The Company Address Form" should {
    "Return an error when both valid postcode and valid country are both present" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addressline1" -> "134 the road",
        "addressline2" -> "Lawley Village",
        "addressline3" -> "Telford",
        "addressline4" -> "Dawley",
        "postcode" -> "TF11 2FT",
        "country" -> "UK"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe ""
          err.message shouldBe Messages("validation.error.countrypostcode")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // model fron json
  "The Company Address Form model" should {
    "load the JSON successfully" in {

      implicit val formats = Json.format[CompanyAddressModel]

      val address = Json.parse(companyAddressJson).as[CompanyAddressModel]
      address.addressline1 shouldBe "line 1"
      address.addressline2 shouldBe "line 2"
      address.addressline3 shouldBe "line 3"
      address.addressline4 shouldBe "line 4"
      address.postcode shouldBe "TF13NY"
      address.country shouldBe "UK"

    }
  }

  // model to json
  "The Company Address Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[CompanyAddressModel]

      val addressJson = Json.toJson(companyAdressModel).toString()
      addressJson shouldBe companyAddressJson

    }
  }

  // form model to json - apply
  "The Company Address Form model" should {
    "call apply corrctly on the model" in {
      implicit val formats = Json.format[CompanyAddressModel]
      val companyAddressForm = CompanyAddressForm.companyAddressForm.fill(companyAdressModel)
      companyAddressForm.get.addressline1 shouldBe "line 1"
      companyAddressForm.get.addressline2 shouldBe "line 2"
      companyAddressForm.get.addressline3 shouldBe "line 3"
      companyAddressForm.get.addressline4 shouldBe "line 4"
      companyAddressForm.get.postcode shouldBe "TF13NY"
      companyAddressForm.get.country shouldBe "UK"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[CompanyAddressModel]
      val companyAddressForm = CompanyAddressForm.companyAddressForm.fill(companyAdressModel)
      val formJson = Json.toJson(companyAddressForm.get).toString()
      formJson shouldBe companyAddressJson
    }
  }
}
