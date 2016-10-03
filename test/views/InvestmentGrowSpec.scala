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

import auth.{Enrolment, Identifier, MockAuthConnector}
import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.{InvestmentGrowController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._

import scala.concurrent.Future

class InvestmentGrowSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  val investmentGrowModel = new InvestmentGrowModel("It will help me to buy tobacco growing facilities")
  val emptyInvestmentGrowModel = new InvestmentGrowModel("")

  class SetupPage {
    val controller = new InvestmentGrowController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector : KeystoreConnector = mockKeyStoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded when coming from WhatWillUse page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST(
        "investmentGrowDesc" -> "It will help me to buy tobacco growing facilities"
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded when coming from PreviousBeforeDOFCS page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.PreviousBeforeDOFCSController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST
      ("investmentGrowDesc" -> "It will help me to buy tobacco growing facilities"))

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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded when coming from NewProduct page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.NewProductController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST
      ("investmentGrowDesc" -> "It will help me to buy tobacco growing facilities"))
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.NewProductController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }


  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded when coming from the SubsidiariesSpendingInvestment page)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.SubsidiariesSpendingInvestmentController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST
      ("investmentGrowDesc" -> "It will help me to buy tobacco growing facilities"))
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }


  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded when coming from the SubsidiariesNinetyOwned page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.SubsidiariesNinetyOwnedController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST
      ("investmentGrowDesc" -> "It will help me to buy tobacco growing facilities"))
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesNinetyOwnedController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page should show an error no data entered" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.submit.apply(authorisedFakeRequestToPOST(
        "investmentGrowDesc" -> ""
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
    document.getElementById("error-summary-display").hasClass("error-summary--show")
  }
}
