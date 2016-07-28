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

package views

import java.util.UUID

import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import controllers.{InvestmentGrowController, routes}
import models.InvestmentGrowModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class InvestmentGrowSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val investmentGrowModel = new InvestmentGrowModel("0;1:23.A,b c")
  val emptyInvestmentGrowModel = new InvestmentGrowModel("")

  class SetupPage {

    val controller = new InvestmentGrowController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The How plan to use investment page" should {

    "Verify that the page contains the correct elements when a valid InvestmentGrowModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "investmentGrowDesc" -> "0;1:23.A,b c"
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.InvestmentGrowController.show.toString()
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    }

    "Verify that the How plan to use investment page contains the correct elements when an invalid InvestmentGrowModel model is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyInvestmentGrowModel)))
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      // Check the error summary is displayed - the whole purpose of this test
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("error-summary-heading").text() shouldBe Messages("common.error.summary.heading")


      // additional page checks to make sure everthing else still as expected if errors on page
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.InvestmentGrowController.show.toString()
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    }

  }

}
