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

package views

import auth.MockAuthConnector
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.{LifetimeAllowanceExceededController, routes}
import models.KiProcessingModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import common.Constants._
import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._

class LifetimeAllowanceExceededSpec extends ViewSpec {

  object TestController extends LifetimeAllowanceExceededController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }


  "The Lifetime Allowance Exceeded notification page" should {

    "Verify that the Lifetime Allowance notification page contains the correct elements and Display an error message for £20 million if knowledge intensive is true" in new Setup {
      val document: Document = {
        when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel]
          (Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Some(kiProcessingModelIsKi)))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.heading")
      document.getElementById("lifetimeExceedReason").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.reason",lifetimeLogicLimitKiToString)
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
      document.getElementById("backInvestmentLink").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.link")
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")

    }
  }


  "The Lifetime Allowance Exceeded notification page" should {

    "Verify that the Lifetime Allowance notification page contains the correct elements and Display an error message for £12 million if knowledge intensive is false" in new Setup {
      val document: Document = {
        when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel]
          (Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Some(kiProcessingModelNotMet)))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.heading")
      document.getElementById("lifetimeExceedReason").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.reason",lifetimeLogicLimitNotKiToString)
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("backInvestmentLink").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.link")

    }
  }
}
