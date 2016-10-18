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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{PreviousBeforeDOFCSController, routes}
import models.{CommercialSaleModel, KiProcessingModel, PreviousBeforeDOFCSModel}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.DateFormatter

import scala.concurrent.Future

class PreviousBeforeDOFCSSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with DateFormatter {

  val mockS4lConnector = mock[S4LConnector]

  val previousBeforeDOFCSModel = new PreviousBeforeDOFCSModel(Constants.StandardRadioButtonYesValue)
  val emptyPreviousBeforeDOFCSModel = new PreviousBeforeDOFCSModel("")
  val kiModel = KiProcessingModel(Some(true),Some(true),Some(true),Some(true),Some(true),Some(true))
  val nonKiModel = KiProcessingModel(Some(false),Some(false),Some(false),Some(false),Some(false),Some(false))
  val commercialSaleYear = 2004
  val commercialSaleMonth = 2
  val commercialSaleDay = 29
  val commercialSaleModel = CommercialSaleModel("true",Some(commercialSaleDay),Some(commercialSaleMonth),Some(commercialSaleYear))
  val commercialDate = toDateString(commercialSaleDay,commercialSaleMonth,commercialSaleYear)
  val secondDate = (difference: Int) => {
    val newDate = new DateTime(commercialSaleYear, commercialSaleMonth, commercialSaleDay, 0, 0).plusYears(difference)
    toDateString(newDate.getDayOfMonth, newDate.getMonthOfYear, newDate.getYear)
  }

  class SetupPage {

    val controller = new PreviousBeforeDOFCSController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  def setup(kiProcessingModel: Option[KiProcessingModel],
            commercialSaleModel: Option[CommercialSaleModel],
            previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel]) : Unit = {
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(previousBeforeDOFCSModel))
  }

  "Verify that the previousBeforeDOFCS page contains the correct elements " +
    "when a valid PreviousBeforeDOFCSModel is passed as returned from keystore" +
    "and the user is KI" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      setup(Some(kiModel),Some(commercialSaleModel),Some(previousBeforeDOFCSModel))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCSLegend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that the previousBeforeDOFCS page contains the correct elements " +
    "when a valid PreviousBeforeDOFCSModel is passed as returned from keystore" +
    "and the user is not KI" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      setup(Some(nonKiModel),Some(commercialSaleModel),Some(previousBeforeDOFCSModel))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsNotKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCSLegend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that previousBeforeDOFCS page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" +
    "and the user is KI" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      setup(Some(kiModel),Some(commercialSaleModel),None)
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-no").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCSLegend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that previousBeforeDOFCS page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" +
    "and the user is not KI" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      setup(Some(nonKiModel),Some(commercialSaleModel),None)
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-no").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsNotKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCSLegend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that PreviousBeforeDOFCS page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      setup(Some(kiModel),Some(commercialSaleModel),None)
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title")

  }
}

