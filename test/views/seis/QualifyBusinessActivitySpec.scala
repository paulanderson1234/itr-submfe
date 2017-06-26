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

package views.seis

import auth.{MockAuthConnector, MockConfigSingleFlow}
import common.KeystoreKeys
import controllers.seis.QualifyBusinessActivityController
import models.{IsFirstTradeModel, QualifyBusinessActivityModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class QualifyBusinessActivitySpec extends ViewSpec {

  object TestController extends QualifyBusinessActivityController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(isQualifyBusinessActivity: Option[QualifyBusinessActivityModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isQualifyBusinessActivity))

  "The Qualify Business Activity page" should {

    "Verify whether the company is qualified for the trading process" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(qualifyBusinessActivityModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      //document.getElementById("isQualifyBusinessActivity-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      //document.getElementById("isQualifyBusinessActivity-noLabel").text() shouldBe Messages("common.radioNoLabel")
      //document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.TradeStartDateController.show().url
      document.body.getElementById("help").text shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.heading")
      document.getElementById("help-text-one").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.one")
      document.getElementById("help-text-two").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.two")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.three")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.four")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.five")
      document.getElementById("help-text-three").text() shouldBe Messages("page.contactInformation.qualifyBusinessActivity.help.text.six")
    }
  }

}
