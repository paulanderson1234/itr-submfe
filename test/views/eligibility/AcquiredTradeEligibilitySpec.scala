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

package views.eligibility

import common.KeystoreKeys
import connectors.KeystoreConnector
import controllers.eligibility.AcquiredTradeEligibilityController
import models.eligibility.AcquiredTradeEligibilityModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import services.TokenService
import views.helpers.ViewSpec

import scala.concurrent.Future

class AcquiredTradeEligibilitySpec extends ViewSpec {

  object TestController extends AcquiredTradeEligibilityController {
    override val keystoreConnector: KeystoreConnector = mock[KeystoreConnector]
    override val tokenService: TokenService = mock[TokenService]

  }

  def setupMocks(throttleCheckPassed: Option[Boolean], acquiredTradeEligibilityModel: Option[AcquiredTradeEligibilityModel] = None): Unit = {
    when(TestController.keystoreConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.throttleCheckPassed))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(throttleCheckPassed))
    when(TestController.keystoreConnector.fetchAndGetFormData[AcquiredTradeEligibilityModel](Matchers.eq(KeystoreKeys.acquiredTradeEligibility))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(acquiredTradeEligibilityModel))
  }

  "The Acquired Trade Eligibility page" should {

    "Verify that the Acquired Trade Eligibility page contains the correct elements when a valid " +
      "AcquiredTradeEligibilityModel is passed from keystore" in {
      val document: Document = {
        setupMocks(Some(true), Some(acquiredTradeYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eligibility.acquiredTrade.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eligibility.acquiredTrade.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("description-one").text() shouldBe Messages("page.eligibility.acquiredTrade.desc")
      document.getElementById("acquiredTrade-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("acquiredTrade-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }


    "Verify that the Acquired Trade Eligibility page contains the correct elements when an invalid AcquiredTradeEligibilityModel is passed" in {
      val document: Document = {
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eligibility.acquiredTrade.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eligibility.acquiredTrade.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("description-one").text() shouldBe Messages("page.eligibility.acquiredTrade.desc")
      document.getElementById("acquiredTrade-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("acquiredTrade-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("acquiredTradeEligibility-error-summary")

    }
  }
}
