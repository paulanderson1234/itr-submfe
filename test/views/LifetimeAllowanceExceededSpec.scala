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

import auth.MockAuthConnector
import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import controllers.{LifetimeAllowanceExceededController, HowToApplyController, routes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class LifetimeAllowanceExceededSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  class SetupPage {

    val controller = new LifetimeAllowanceExceededController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }


  "The Lifetime Allowance Exceeded notification page" should {

    "Verify that the Lifetime Allowance notification page contains the correct elements" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.heading")
      document.getElementById("lifetimeExceedReason").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.reason")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show.toString()
      document.getElementById("backInvestmentLink").text() shouldBe Messages("page.investment.LifetimeAllowanceExceeded.link")
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")

    }
  }
}
