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
import controllers.eisseis.OperatingCostsController
import controllers.routes
import models.OperatingCostsModel
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

class OperatingCostsSpec extends ViewSpec {
  
  object TestController extends OperatingCostsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(operatingCostsModel: Option[OperatingCostsModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(operatingCostsModel))

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "Verify that the OperatingCosts page contains the correct elements " +
    "when a valid OperatingCostsModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(operatingCostsModel))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.IsKnowledgeIntensiveController.show().url
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.OperatingCosts.heading")
    document.getElementById("operating-costs-hint-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.one")
    document.getElementById("operating-costs-hint-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.two")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.one")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.two")
    document.getElementById("col-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.three")
    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")

  }

  "Verify that OperatingCosts page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.IsKnowledgeIntensiveController.show().url
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.OperatingCosts.heading")
    document.getElementById("operating-costs-hint-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.one")
    document.getElementById("operating-costs-hint-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.two")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.one")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.two")
    document.getElementById("col-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.three")
    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.three")
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
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")

  }
}
