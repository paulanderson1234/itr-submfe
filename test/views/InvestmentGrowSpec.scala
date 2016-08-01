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

import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
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

  //todo Change to Subsidiaries90Owned when created
  val subsidiariesNinetyOwned = new SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesSpendingInvestment = new SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonNoValue)
  val newProductModel = new NewProductModel(Constants.StandardRadioButtonYesValue)
  val previousBeforeDOFCSModel= new PreviousBeforeDOFCSModel(Constants.StandardRadioButtonYesValue)
  val whatWilUseForModel = new WhatWillUseForModel("Tobacco growing")

  class SetupPage {
    val controller = new InvestmentGrowController{
      val keyStoreConnector : KeystoreConnector = mockKeyStoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = null, subsidiariesSpendingInvestment = null, newProduct = null, previousDOFCS = null " +
    "and whatWillUseFor = null" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show.toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = null, subsidiariesSpendingInvestment = null, newProduct = null, previousDOFCS = null " +
    "and whatWillUseFor = Some(whatWIllUseFor)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show.toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = null, subsidiariesSpendingInvestment = null, newProduct = null, previousDOFCS = Some(previousBeforeDOFCS) " +
    "and whatWillUseFor = Some(whatWIllUseFor)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show.toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }


  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = null, subsidiariesSpendingInvestment = null, newProduct = Some(newProduct), previousDOFCS = Some(previousBeforeDOFCS) " +
    "and whatWillUseFor = Some(whatWIllUseFor)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(newProductModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.NewProductController.show.toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }


  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = null, subsidiariesSpendingInvestment = Some(subsidiariesSpendingInvestment), " +
    "newProduct = Some(newProduct), previousDOFCS = Some(previousBeforeDOFCS)" +
    "and whatWillUseFor = Some(whatWIllUseFor)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(subsidiariesSpendingInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(newProductModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show.toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }

  "The InvestmentGrow Page" +
    "Verify that the correct elements are loaded " +
    "When newGeographicMarket = Some(newGeographicMarket), subsidiariesSpendingInvestment = Some(subsidiariesSpendingInvestment), " +
    "newProduct = Some(newProduct), previousDOFCS = Some(previousBeforeDOFCS)" +
    "and whatWillUseFor = Some(whatWIllUseFor)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel]
        (Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any())).
        thenReturn(Future.successful(Some(subsidiariesSpendingInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(newProductModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
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
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString()
    document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    document.body.getElementById("investmentGrowDesc").hasClass("form-control")
    document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
    document.getElementById("labelTextId").hasClass("visuallyhidden")
  }
}
