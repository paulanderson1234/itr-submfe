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
import controllers.eligibility.GroupsAndSubsEligibilityController
import models.eligibility.GroupsAndSubsEligibilityModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class GroupsAndSubsEligibilitySpec extends ViewSpec {

  object TestController extends GroupsAndSubsEligibilityController {
    override val keystoreConnector: KeystoreConnector = mock[KeystoreConnector]
  }

  def setupMocks(groupsAndSubsEligibilityModel: Option[GroupsAndSubsEligibilityModel] = None): Unit =
    when(TestController.keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](Matchers.eq(KeystoreKeys.groupsAndSubsEligibility))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(groupsAndSubsEligibilityModel))

  "The Groups And Subs Eligibility page" should {

    "Verify that the Groups And Subs Eligibility page contains the correct elements when a valid " +
      "GroupsAndSubsEligibilityModel is passed from keystore" in {
      val document: Document = {
        setupMocks(Some(groupOrSubYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eligibility.groupsAndSubs.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eligibility.groupsAndSubs.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("description-one").text() shouldBe Messages("page.eligibility.groupsAndSubs.desc")
      document.getElementById("bullet-one").text() shouldBe Messages("page.eligibility.groupsAndSubs.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.eligibility.groupsAndSubs.bullet.two")
      document.getElementById("isGroupOrSub-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isGroupOrSub-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }


    "Verify that the Groups And Subs Eligibility page contains the correct elements when an invalid GroupsAndSubsEligibilityModel is passed" in {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.eligibility.groupsAndSubs.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.eligibility.groupsAndSubs.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("description-one").text() shouldBe Messages("page.eligibility.groupsAndSubs.desc")
      document.getElementById("bullet-one").text() shouldBe Messages("page.eligibility.groupsAndSubs.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.eligibility.groupsAndSubs.bullet.two")
      document.getElementById("isGroupOrSub-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isGroupOrSub-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("groupsAndSubsEligibility-error-summary")

    }
  }
}
