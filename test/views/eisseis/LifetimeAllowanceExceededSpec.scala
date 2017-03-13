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
import common.Constants._
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.LifetimeAllowanceExceededController
import models.KiProcessingModel
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

class LifetimeAllowanceExceededSpec extends ViewSpec {

  object TestController extends LifetimeAllowanceExceededController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(kiProcessingModel: Option[KiProcessingModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))

    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel]
      (Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(kiProcessingModelNotMet)))

    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
  }

  "The Lifetime Allowance Exceeded notification page" should {

    "Verify that the Lifetime Allowance notification page contains the correct elements and Display an error message for £20 million if knowledge intensive is true" in new Setup {
      val document: Document = {
        setupMocks(Some(kiProcessingModelIsKi))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.heading")
      document.getElementById("lifetimeExceedReason").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.reason", lifetimeLogicLimitKiToString)
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url

      document.getElementById("change-answers").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.text") +
        " " + Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.link") + "."
      document.getElementById("change-answers-link").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.link")
      document.getElementById("change-answers-link").attr("href") shouldBe controllers.eisseis.routes.ReviewPreviousSchemesController.show().url

      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")

    }
  }


  "The Lifetime Allowance Exceeded notification page" should {

    "Verify that the Lifetime Allowance notification page contains the correct elements and Display an error message for £12 million if knowledge intensive is false" in new Setup {
      val document: Document = {
        setupMocks(Some(kiProcessingModelNotMet))

        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.heading")
      document.getElementById("lifetimeExceedReason").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.reason",lifetimeLogicLimitNotKiToString)
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url

      document.getElementById("change-answers").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.text") +
        " " + Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.link") + "."
      document.getElementById("change-answers-link").text() shouldBe Messages("page.eisseis.investment.LifetimeAllowanceExceeded.change.link")
      document.getElementById("change-answers-link").attr("href") shouldBe controllers.eisseis.routes.ReviewPreviousSchemesController.show().url

      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")

    }
  }
}
