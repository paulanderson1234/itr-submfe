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
import controllers.eisseis.TradingForTooLongController
import controllers.routes
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

class TradingForTooLongSpec extends ViewSpec {

  object TestController extends TradingForTooLongController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Trading for too long error page" should {

    "Verify that start page contains the correct elements" in new Setup {

      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))

      val document: Document = {
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title shouldEqual Messages("page.eisseis.investment.TradingForTooLong.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.investment.TradingForTooLong.heading")
      document.body.getElementById("trading-too-long-reason").text() shouldEqual Messages("page.eisseis.investment.TradingForTooLong.reason")
      document.body.getElementById("trading-too-long").text() shouldEqual Messages("page.eisseis.investment.TradingForTooLong.bullet.one")
      document.body.getElementById("not-new-business").text() shouldEqual Messages("page.eisseis.investment.TradingForTooLong.bullet.two")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.NewProductController.show().url

    }
  }
}
