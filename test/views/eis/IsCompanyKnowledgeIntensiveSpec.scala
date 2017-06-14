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

import auth.{MockConfigEISFlow, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eis.IsCompanyKnowledgeIntensiveController
import models.IsCompanyKnowledgeIntensiveModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class IsCompanyKnowledgeIntensiveSpec extends ViewSpec {

  object TestController extends IsCompanyKnowledgeIntensiveController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(isCompanyKnowledgeIntensiveModel: Option[IsCompanyKnowledgeIntensiveModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[IsCompanyKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isCompanyKnowledgeIntensive))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isCompanyKnowledgeIntensiveModel))

  "Verify that the isCompanyKnowledgeIntensive page contains the correct elements " +
    "when a valid IsCompanyKnowledgeIntensiveModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(isCompanyKnowledgeIntensiveModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url
    document.title() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.heading")
    document.getElementById("description-ki").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.description.1")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-four")
    document.select("#isCompanyKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isCompanyKnowledgeIntensive-no").size() shouldBe 1
    document.getElementById("isCompanyKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isCompanyKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that isCompanyKnowledgeIntensive page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url
    document.title() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.heading")
    document.getElementById("description-ki").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.description.1")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.ki-requirement-four")
    document.select("#isCompanyKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isCompanyKnowledgeIntensive-no").size() shouldBe 1
    document.getElementById("isCompanyKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isCompanyKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that IsCompanyKnowledgeIntensive page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.companyDetails.IsCompanyKnowledgeIntensive.title")
  }
}
