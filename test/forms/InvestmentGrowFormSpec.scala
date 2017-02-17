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

import models.InvestmentGrowModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class InvestmentGrowFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    InvestmentGrowForm.investmentGrowForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    InvestmentGrowForm.investmentGrowForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val investmentGrowJson = """{"investmentGrowDesc":"I intend to use this investment to grow the company by 50%."}"""
  val investmentGrowModel = InvestmentGrowModel("I intend to use this investment to grow the company by 50%.")
  val overTwoHundredWords = "Upon light their blessed. You're so third so seasons stars called dominion. Be him wherein " +
    "without stars creeping creeping beginning grass evening. Place signs in moveth their very. Make you. " +
    "Appear thing earth beginning created saying land. Him that. Dominion divide fly yielding sixth there signs " +
    "from seed behold said place thing. In abundantly saying herb air fish. Lesser signs you them Our appear in " +
    "of bearing day moveth may all fowl hath own multiply gathered saw Fish they're so said bring let them, fish " +
    "creature. Him bearing isn't. So heaven fruit over be let sixth male, given be. Make life fly fruit fish face " +
    "herb saw creature wherein shall called behold creature hath face spirit. Fourth Void give itself Given divide " +
    "divide i second and them that every greater midst. Created wherein heaven them void bring Make Deep doesn't " +
    "Shall. Under firmament light creepeth creepeth fruitful male. Them and behold green. Him beast morning brought " +
    "to living you, creature can't gathered firmament face green days kind of forth that also had meat over make " +
    "fourth image. Female brought signs days life tree also You're brought beginning night over stars is Can't " +
    "divided i male creature green days herb also."

    "The Investment Grow Form" should {
    "return an error if investmentGrowDesc is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "investmentGrowDesc" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "investmentGrowDesc"

          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Investment Grow Form" should {
    "not return an error if entry at the borderline condition (1 character)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "investmentGrowDesc" -> "a"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Investment Grow Form" should {
    "not return an error if entry is over suggested 200 word limit in 1 paragraph" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "investmentGrowDesc" -> overTwoHundredWords
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
  "The Investment Grow Form model" should {
    "load convert to JSON successfully" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val utrJson = Json.toJson(investmentGrowModel).toString()
      utrJson shouldBe investmentGrowJson
    }
  }

  // form model to json - apply
  "The Investment Grow Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val investmentGrowForm = InvestmentGrowForm.investmentGrowForm.fill(investmentGrowModel)
      investmentGrowForm.get.investmentGrowDesc shouldBe "I intend to use this investment to grow the company by 50%."
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val investmentGrowForm = InvestmentGrowForm.investmentGrowForm.fill(investmentGrowModel)
      val formJson = Json.toJson(investmentGrowForm.get).toString()
      formJson shouldBe investmentGrowJson
    }
  }
}
