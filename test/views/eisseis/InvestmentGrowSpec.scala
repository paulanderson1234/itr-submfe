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

package views.eisseis

import auth.{MockConfig, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.InvestmentGrowController
import controllers.routes
import models._
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class InvestmentGrowSpec extends ViewSpec {

  object TestController extends InvestmentGrowController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(investmentGrowModel: Option[InvestmentGrowModel] = None, newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None,
                 newProductModel: Option[NewProductModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(investmentGrowModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "The InvestmentGrow Page" should {

    "Verify that the correct elements are loaded when coming from WhatWillUse page" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel),backLink = Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when coming from PreviousBeforeDOFCS page" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel),backLink = Some(controllers.eisseis.routes.PreviousBeforeDOFCSController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.PreviousBeforeDOFCSController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when coming from NewProduct page" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel), backLink = Some(controllers.eisseis.routes.NewProductController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.NewProductController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when coming from the SubsidiariesSpendingInvestment page)" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel), backLink = Some(controllers.eisseis.routes.SubsidiariesSpendingInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.SubsidiariesSpendingInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }


    "Verify that the correct elements are loaded when coming from the SubsidiariesNinetyOwned page" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel), backLink = Some(controllers.eisseis.routes.SubsidiariesNinetyOwnedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.SubsidiariesNinetyOwnedController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when hasGeoMarket is true and hasNewProduct is true" in new Setup{
      val document: Document = {
        setupMocks(Some(investmentGrowModel),Some(newGeographicalMarketModelYes),
          Some(newProductMarketModelYes),Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 2
      document.getElementById("bullet-geographical-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.geographicalMarket")
      document.getElementById("bullet-product-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.productMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")

      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when hasGeoMarket is true and hasNewProduct is false" in new Setup{
      val document: Document = {
        setupMocks(Some(investmentGrowModel),Some(newGeographicalMarketModelYes),
          Some(newProductMarketModelNo),Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 1
      document.getElementById("bullet-geographical-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.geographicalMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when hasGeoMarket is false and hasNewProduct is true" in new Setup{
      val document: Document = {
        setupMocks(Some(investmentGrowModel),Some(newGeographicalMarketModelNo),
          Some(newProductMarketModelYes),Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 1
      document.getElementById("bullet-product-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.productMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when hasGeoMarket is false and hasNewProduct is false" in new Setup{
      val document: Document = {
        setupMocks(Some(investmentGrowModel),Some(newGeographicalMarketModelNo),
          Some(newProductMarketModelNo),Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "Verify that the correct elements are loaded when newGeoMarket is not defined and hasNewProduct is not defined" in new Setup{
      val document: Document = {
        setupMocks(investmentGrowModel = Some(investmentGrowModel), backLink = Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
    }

    "show an error no data entered" in new Setup {
      val document: Document = {
        setupMocks(backLink = Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("investmentGrowDesc").hasClass("form-control")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }
  }
}
