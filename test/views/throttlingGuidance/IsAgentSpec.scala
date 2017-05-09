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

package views.throttlingGuidance

import common.KeystoreKeys
import controllers.throttlingGuidance.IsAgentController
import models.throttlingGuidance.IsAgentModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class IsAgentSpec extends ViewSpec {

  val isAgentModelInvalidYes = new IsAgentModel("")

  object TestController extends IsAgentController {
    override lazy val keystoreConnector = mockKeystoreConnector
  }

  def setupMocks(isAgentModel: Option[IsAgentModel] = None): Unit =
    when(mockKeystoreConnector.fetchAndGetFormData[IsAgentModel](Matchers.eq(KeystoreKeys.isAgent))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(isAgentModel))

      when(mockKeystoreConnector.saveFormData(Matchers.eq(KeystoreKeys.isAgent),
        Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(CacheMap("", Map())))

  "The Trade Start Date page" should {

    "Verify that the Trade start date  page contains the correct elements when a valid 'Yes' IsAgentModel is passed" in {
      val document: Document = {
        setupMocks(Some(isAgentModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.throttlingGuidance.Agent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.Agent.heading")
      document.getElementById("isAgentLegend-legend").hasClass("visuallyhidden")
      document.getElementById("isAgent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isAgent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }


    "Verify that the Trade start date  page contains the correct elements when a valid 'No' IsAgentModel is passed" in {
      val document: Document = {
        setupMocks(Some(isAgentModelNo))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.throttlingGuidance.Agent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.Agent.heading")
      document.getElementById("isAgentLegend-legend").hasClass("visuallyhidden")
      document.getElementById("isAgent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isAgent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")

    }

    "Verify that the Trade start date  page contains the correct elements when an invalid IsAgentModel is passed" in  {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.throttlingGuidance.Agent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.Agent.heading")
      document.getElementById("isAgentLegend-legend").hasClass("visuallyhidden")
      document.getElementById("isAgent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isAgent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the Trade start date  page contains the correct elements when an invalid IsAgentYesModel is passed" in {
      val document: Document = {
        setupMocks(Some(isAgentModelInvalidYes))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.throttlingGuidance.Agent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.Agent.heading")
      document.getElementById("isAgentLegend-legend").hasClass("visuallyhidden")
      document.getElementById("isAgent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isAgent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
