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

package views.seis

import models.submission.SchemeTypesModel
import models.{CheckAnswersModel, DateOfIncorporationModel, TradeStartDateModel}
import models.seis.SEISCheckAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.CheckAnswersSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.checkAndSubmit.CheckAnswers

class CheckAnswersCompanyDetailsSpec extends CheckAnswersSpec {

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models and had trade start date is true" in {
      val model = SEISCheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(tradeStartDateModelYes),
        Some(natureOfBusinessModel), Some(subsidiariesModelNo), None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        model.natureOfBusinessModel.get.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.seis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(model.dateOfIncorporationModel.get.day.get,
          model.dateOfIncorporationModel.get.month.get, model.dateOfIncorporationModel.get.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      //Trade start date
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-Question0").text() shouldBe
        Messages("summaryQuestion.hasTradeStartDate")
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-Answer0").text() shouldBe
        model.tradeStartDateModel.get.hasTradeStartDate
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-Question1").text() shouldBe
        Messages("summaryQuestion.tradeStartDate")
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-Answer1").text() shouldBe
        TradeStartDateModel.toDateString(model.tradeStartDateModel.get.tradeStartDay.get,
          model.tradeStartDateModel.get.tradeStartMonth.get, model.tradeStartDateModel.get.tradeStartYear.get)
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-link")
        .attr("href") shouldEqual controllers.seis.routes.TradeStartDateController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsController.show().url
    }

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models and had trade start date is false" in {
      val model = SEISCheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(tradeStartDateModelNo),
        Some(natureOfBusinessModel), Some(subsidiariesModelNo), None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        model.natureOfBusinessModel.get.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.seis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(model.dateOfIncorporationModel.get.day.get,
          model.dateOfIncorporationModel.get.month.get, model.dateOfIncorporationModel.get.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      //Trade start date
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-question").text() shouldBe
        Messages("summaryQuestion.hasTradeStartDate")
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-answer").text() shouldBe
        model.tradeStartDateModel.get.hasTradeStartDate
      companyDetailsTableTBody.select("tr").get(2).getElementById("tradeStart-link")
        .attr("href") shouldEqual controllers.seis.routes.TradeStartDateController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsController.show().url
    }

    "Verify that the Check Answers page contains an empty table for Section 1: Company details" +
      " when an empty set of company detail models are passed" in {
      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      companyDetailsTableTBody.select("tr").size() shouldBe 0

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsController.show().url
    }
  }


  "The Check Answers page" should {

    "Verify that the scheme description contains only SEIS" in {

      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      document.getElementById("schemes").children().size() shouldBe 1
      document.getElementById("seis-scheme").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.scheme.seis")

      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      companyDetailsTableTBody.select("tr").size() shouldBe 0

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsController.show().url
    }
  }

}

