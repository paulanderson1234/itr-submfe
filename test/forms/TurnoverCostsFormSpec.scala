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

import models.AnnualTurnoverCostsModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class TurnoverCostsFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    TurnoverCostsForm.turnoverCostsForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    TurnoverCostsForm.turnoverCostsForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors.head),
      userData => None
    )
  }

  val turnoverCostsJson = {
    """{
      |"amount1":"750000",
      |"amount2":"800000",
      |"amount3":"934000",
      |"amount4":"231000",
      |"amount5":"340000",
      |"firstYear":"2003",
      |"secondYear":"2004",
      |"thirdYear":"2005",
      |"fourthYear":"2006",
      |"fifthYear":"2007"}""".stripMargin}
  val turnoverCostsModel = AnnualTurnoverCostsModel("750000", "800000", "934000", "231000", "340000", "2003", "2004", "2005", "2006", "2007")

  // turnover Costs 1st Year validation
  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 1st Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount1"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 1st Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "ABCD",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount1"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 1st Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "100ABCD",
        "amount2" -> "231000",
        "amount3" -> "800000",
        "amount4" -> "934000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount1"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "not return an error if Turnover Costs 1st Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "999999999",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 1st Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "9999999991",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "amount1"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.one")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 1st Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "0",
        "amount2" -> "555555",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount1"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.one")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  // turnover Costs 2nd Year validation
  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 2nd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount2"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 2nd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "876600",
        "amount2" -> "ABCD",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount2"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 2nd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "100ABCD",
        "amount3" -> "800000",
        "amount4" -> "934000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount2"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "not return an error if Turnover Costs 2nd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "999999999",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 2nd Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "9999999991",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "amount2"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.two")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 2nd Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "555555",
        "amount2" -> "0",
        "amount3" -> "934000",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount2"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.two")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  // turnover Costs 3rd Year validation
  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 3rd Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "934000",
        "amount3" -> "",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount3"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 3rd Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "876600",
        "amount2" -> "934000",
        "amount3" -> "ABCD",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount3"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 3rd Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "100ABCD",
        "amount4" -> "934000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount3"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "not return an error if Turnover Costs 3rd Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "800000",
        "amount3" -> "999999999",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 3rd Year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "800000",
        "amount3" -> "9999999991",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "amount3"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.three")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Turnover Costs 3rd Year is 0" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "555555",
        "amount2" -> "555555",
        "amount3" -> "0",
        "amount4" -> "231000",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount3"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.three")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "Return an error if Research and Development Costs 4th Year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount4"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Research and Development Costs 4th Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "ABCD",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount4"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if Research and Development Costs 4th Year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "100ABCD",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount4"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.four")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "not return an error if Research and Development 4th Year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "999999999",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if turnover costs 4th year regex pattern is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "9999999991",
        "amount5" -> "340000",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "amount4"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.four")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  //Research and Development Costs 5th Year validation
  "The Turnover Costs Form" should {
    "Return an error if turnover costs 5th year is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "800000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "340000",
        "amount5" -> "",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount5"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if turnover costs 5th year Year contains letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "340000",
        "amount5" -> "ABCD",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount5"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if turnover costs 5th year contains numbers and letters" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "340000",
        "amount5" -> "100ABCD",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) =>
          err.key shouldBe "amount5"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.five")
          err.args shouldBe Array()
        case _ =>
          fail("Missing error")
      }
    }
  }


  "The Turnover Costs Form" should {
    "not return an error if turnover costs 5th year regex pattern is at the borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "340000",
        "amount5" -> "999999999",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Turnover Costs Form" should {
    "Return an error if turnover costs 5th yearn is above borderline condition" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "amount1" -> "231000",
        "amount2" -> "800000",
        "amount3" -> "934000",
        "amount4" -> "340000",
        "amount5" -> "9999999991",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "amount5"
          err.message shouldBe Messages("page.companyDetails.TurnoverCosts.error.field.five")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }


  // model fron json
  "The Turnover Costs Form model" should {
    "load the JSON successfully" in {

      implicit val formats = Json.format[AnnualTurnoverCostsModel]

      val turnoverCosts = Json.parse(turnoverCostsJson).as[AnnualTurnoverCostsModel]
      turnoverCosts.amount1 shouldBe "750000"
      turnoverCosts.amount2 shouldBe "800000"
      turnoverCosts.amount3 shouldBe "934000"
      turnoverCosts.amount4 shouldBe "231000"
      turnoverCosts.amount5 shouldBe "340000"

    }
  }

  // model to json
  "The Turnover Costs Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[AnnualTurnoverCostsModel]

      val addressJson = Json.toJson(turnoverCostsModel)
      addressJson shouldBe Json.parse(turnoverCostsJson)

    }
  }

  // form model to json - apply
  "The Turnover Costs Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[AnnualTurnoverCostsModel]
      val turnoverCostsForm = TurnoverCostsForm.turnoverCostsForm.fill(turnoverCostsModel)
      turnoverCostsForm.get.amount1 shouldBe "750000"
      turnoverCostsForm.get.amount2 shouldBe "800000"
      turnoverCostsForm.get.amount3 shouldBe "934000"
      turnoverCostsForm.get.amount4 shouldBe "231000"
      turnoverCostsForm.get.amount5 shouldBe "340000"
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[AnnualTurnoverCostsModel]
      val turnoverCostsForm = TurnoverCostsForm.turnoverCostsForm.fill(turnoverCostsModel)
      val formJson = Json.toJson(turnoverCostsForm.get)
      formJson shouldBe Json.parse(turnoverCostsJson)
    }
  }
}
