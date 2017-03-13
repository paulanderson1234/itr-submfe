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
import controllers.eisseis.IsKnowledgeIntensiveController
import controllers.routes
import models.IsKnowledgeIntensiveModel
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class IsKnowledgeIntensiveSpec extends ViewSpec {

  object TestController extends IsKnowledgeIntensiveController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(isKnowledgeIntensiveModel))

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "Verify that the isKnowledgeIntensive page contains the correct elements " +
    "when a valid IsKnowledgeIntensiveModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(isKnowledgeIntensiveModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.CommercialSaleController.show().url
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.heading")
    document.getElementById("ki-requirement-definition").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-definition")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-four")
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.getElementById("isKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that isKnowledgeIntensive page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.CommercialSaleController.show().url
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.heading")
    document.getElementById("ki-requirement-definition").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-definition")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-four")
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.getElementById("isKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that IsKnowledgeIntensive page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    val document : Document = {
      setupMocks()
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
  }
}
