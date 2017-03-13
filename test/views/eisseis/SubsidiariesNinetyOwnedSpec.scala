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
import controllers.eisseis.SubsidiariesNinetyOwnedController
import controllers.routes
import models.SubsidiariesNinetyOwnedModel
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

class SubsidiariesNinetyOwnedSpec extends ViewSpec {

  object TestController extends SubsidiariesNinetyOwnedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }
  
  def setupMocks(subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwnedModel))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "The Subsidiaries Ninety Owned page" should {

    "Verify that the Subsidiaries Ninety Owned page contains the correct elements when a valid SubsidiariesNinetyOwnedModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(subsidiariesNinetyOwnedModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.heading")
      document.getElementById("text-one-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.one")
      document.getElementById("text-two-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.SubsidiariesSpendingInvestmentController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }

    "Verify that the taxpayer reference page contains the correct elements when an invalid TaxpayerReferenceModel is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.heading")
      document.getElementById("text-one-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.one")
      document.getElementById("text-two-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.SubsidiariesSpendingInvestmentController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }
  }
}
