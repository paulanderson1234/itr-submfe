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

import models.OperatingCostsModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class OperatingCostsFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    OperatingCostsForm.operatingCostsForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    OperatingCostsForm.operatingCostsForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors.head),
      userData => None
    )
  }

  val operatingCostsJson =
    """{"operatingCosts1stYear":"750000",
      |"operatingCosts2ndYear":"800000",
      |"operatingCosts3rdYear":"934000",
      |"rAndDCosts1stYear":"231000",
      |"rAndDCosts2ndYear":"340000",
      |"rAndDCosts3rdYear":"344000",
      |"firstYear":"2005",
      |"secondYear":"2004",
      |"thirdYear":"2003"
      |}""".stripMargin
  val operatingCostsModel = OperatingCostsModel("750000", "800000", "934000", "231000", "340000", "344000", "2005", "2004", "2003")

  // operating Costs 1st Year validation
  "The Operating Costs Form" should {
    "Return an error if Operating Costs 1st Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 1st Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "ABCD",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 1st Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100ABCD",
        "operatingCosts2ndYear" -> "231000",
        "operatingCosts3rdYear" -> "800000",
        "rAndDCosts1stYear" -> "934000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Operating Costs 1st Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "999999999",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 1st Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "9999999991",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "operatingCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.one")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 1st Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "0",
        "operatingCosts2ndYear" -> "555555",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  // operating Costs 2nd Year validation
  "The Operating Costs Form" should {
    "Return an error if Operating Costs 2nd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 2nd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "876600",
        "operatingCosts2ndYear" -> "ABCD",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 2nd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "100ABCD",
        "operatingCosts3rdYear" -> "800000",
        "rAndDCosts1stYear" -> "934000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Operating Costs 2nd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "999999999",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 2nd Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "9999999991",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "operatingCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.two")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 2nd Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "555555",
        "operatingCosts2ndYear" -> "0",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  // operating Costs 3rd Year validation
  "The Operating Costs Form" should {
    "Return an error if Operating Costs 3rd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "934000",
        "operatingCosts3rdYear" -> "",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 3rd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "876600",
        "operatingCosts2ndYear" -> "934000",
        "operatingCosts3rdYear" -> "ABCD",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 3rd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "100ABCD",
        "rAndDCosts1stYear" -> "934000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Operating Costs 3rd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "999999999",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 3rd Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "9999999991",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "operatingCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.three")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Operating Costs 3rd Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "555555",
        "operatingCosts2ndYear" -> "555555",
        "operatingCosts3rdYear" -> "0",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "operatingCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  //Research and Development Costs 1st Year validation
  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 1st Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 1st Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "ABCD",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 1st Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "100ABCD",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Research and Development 1st Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "999999999",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development 1st regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "9999999991",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "rAndDCosts1stYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.four")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  //Research and Development Costs 2nd Year validation
  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 2nd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 2nd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "ABCD",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 2nd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "100ABCD",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Research and Development 2nd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "999999999",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development 2nd regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "9999999991",
        "rAndDCosts3rdYear" -> "344000",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "rAndDCosts2ndYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.five")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  //Research and Development Costs 3rd Year validation
  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 3rd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "800000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "344000",
        "rAndDCosts3rdYear" -> "",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.six")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 3rd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "344000",
        "rAndDCosts3rdYear" -> "ABCD",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.six")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development Costs 3rd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "344000",
        "rAndDCosts3rdYear" -> "100ABCD",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "rAndDCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.six")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Operating Costs Form" should {
    "not return an error if Research and Development 3rd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "344000",
        "rAndDCosts3rdYear" -> "999999999",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Operating Costs Form" should {
    "Return an error if Research and Development 3rd regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "231000",
        "operatingCosts2ndYear" -> "800000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "340000",
        "rAndDCosts2ndYear" -> "344000",
        "rAndDCosts3rdYear" -> "9999999991",
        "firstYear" -> "2005",
        "secondYear" -> "2004",
        "thirdYear" -> "2003"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "rAndDCosts3rdYear"
          err.message shouldBe Messages("page.companyDetails.OperatingCosts.error.field.six")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }


  // model fron json
  "The Operating Costs Form model" should {
    "load the JSON successfully" in {

      implicit val formats = Json.format[OperatingCostsModel]

      val operatingCosts = Json.parse(operatingCostsJson).as[OperatingCostsModel]
      operatingCosts.operatingCosts1stYear shouldBe "750000"
      operatingCosts.operatingCosts2ndYear shouldBe "800000"
      operatingCosts.operatingCosts3rdYear shouldBe "934000"
      operatingCosts.rAndDCosts1stYear shouldBe "231000"
      operatingCosts.rAndDCosts2ndYear shouldBe "340000"
      operatingCosts.rAndDCosts3rdYear shouldBe "344000"
      operatingCosts.firstYear shouldBe "2005"
      operatingCosts.secondYear shouldBe "2004"
      operatingCosts.thirdYear shouldBe "2003"

    }
  }

  // model to json
  "The Operating Costs Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[OperatingCostsModel]

      val addressJson = Json.toJson(operatingCostsModel)
      addressJson shouldBe Json.parse(operatingCostsJson)

    }
  }

  // form model to json - apply
  "The Operating Costs Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[OperatingCostsModel]
      val operatingCostsForm = OperatingCostsForm.operatingCostsForm.fill(operatingCostsModel)
      operatingCostsForm.get.operatingCosts1stYear shouldBe "750000"
      operatingCostsForm.get.operatingCosts2ndYear shouldBe "800000"
      operatingCostsForm.get.operatingCosts3rdYear shouldBe "934000"
      operatingCostsForm.get.rAndDCosts1stYear shouldBe "231000"
      operatingCostsForm.get.rAndDCosts2ndYear shouldBe "340000"
      operatingCostsForm.get.rAndDCosts3rdYear shouldBe "344000"
      operatingCostsForm.get.firstYear shouldBe "2005"
      operatingCostsForm.get.secondYear shouldBe "2004"
      operatingCostsForm.get.thirdYear shouldBe "2003"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[OperatingCostsModel]
      val operatingCostsForm = OperatingCostsForm.operatingCostsForm.fill(operatingCostsModel)
      val formJson = Json.toJson(operatingCostsForm.get)
      formJson shouldBe Json.parse(operatingCostsJson)
    }
  }
}
