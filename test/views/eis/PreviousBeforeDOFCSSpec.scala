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

package views.eis

import auth.MockAuthConnector
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import controllers.eis.PreviousBeforeDOFCSController
import models.{CommercialSaleModel, KiProcessingModel, PreviousBeforeDOFCSModel}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import utils.DateFormatter
import views.helpers.ViewSpec

import scala.concurrent.Future

class PreviousBeforeDOFCSSpec extends ViewSpec with DateFormatter {

  val kiModel = KiProcessingModel(Some(true),Some(true),Some(true),Some(true),Some(true),Some(true))
  val nonKiModel = KiProcessingModel(Some(false),Some(false),Some(false),Some(false),Some(false),Some(false))
  val commercialDate = toDateString(commercialSaleDay,commercialSaleMonth,commercialSaleYear)
  val secondDate = (difference: Int) => {
    val newDate = new DateTime(commercialSaleYear, commercialSaleMonth, commercialSaleDay, 0, 0).plusYears(difference)
    toDateString(newDate.getDayOfMonth, newDate.getMonthOfYear, newDate.getYear)
  }

  object TestController extends PreviousBeforeDOFCSController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(kiProcessingModel: Option[KiProcessingModel] = None, commercialSaleModel: Option[CommercialSaleModel] = None,
                 previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None) : Unit = {
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCSModel))
  }

  "Verify that the previousBeforeDOFCS page contains the correct elements " +
    "when a valid PreviousBeforeDOFCSModel is passed as returned from keystore" +
    "and the user is KI" in new Setup {
    val document : Document = {
      setupMocks(Some(kiModel),Some(commercialSaleModelYes),Some(previousBeforeDOFCSModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.UsedInvestmentReasonBeforeController.show().url
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title","29 February 2004","28 February 2014")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCS-legend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that the previousBeforeDOFCS page contains the correct elements " +
    "when a valid PreviousBeforeDOFCSModel is passed as returned from keystore" +
    "and the user is not KI" in new Setup {
    val document : Document = {
      setupMocks(Some(nonKiModel),Some(commercialSaleModelYes),Some(previousBeforeDOFCSModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.UsedInvestmentReasonBeforeController.show().url
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title", "29 February 2004","28 February 2011")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsNotKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCS-legend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that previousBeforeDOFCS page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" +
    "and the user is KI" in new Setup {
    val document : Document = {
      setupMocks(Some(kiModel),Some(commercialSaleModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.UsedInvestmentReasonBeforeController.show().url
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title", "29 February 2004","28 February 2014")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-no").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCS-legend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that previousBeforeDOFCS page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" +
    "and the user is not KI" in new Setup {
    val document : Document = {
      setupMocks(Some(nonKiModel),Some(commercialSaleModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.UsedInvestmentReasonBeforeController.show().url
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title", "29 February 2004","28 February 2011")
    document.getElementById("main-heading").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.select("#previousBeforeDOFCS-yes").size() shouldBe 1
    document.select("#previousBeforeDOFCS-no").size() shouldBe 1
    document.getElementById("previousBeforeDOFCS-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("previousBeforeDOFCS-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("previousBeforeDOFCS").getElementsByClass("form-hint").first().text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.description",Constants.IsNotKnowledgeIntensiveYears)
    document.getElementById("previousBeforeDOFCS-legend").select(".visuallyhidden").text() shouldBe
      Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,secondDate(Constants.IsNotKnowledgeIntensiveYears))
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that PreviousBeforeDOFCS page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    val document : Document = {
      setupMocks(Some(kiModel),Some(commercialSaleModelYes))
      // submit the model with no radio slected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.previousInvestment.previousBeforeDOFCS.title", "29 February 2004","28 February 2014")

  }
}
