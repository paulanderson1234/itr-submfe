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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package views

import java.util.UUID

import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.{NewGeographicalMarketController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._

import scala.concurrent.Future

class NewGeographicalMarketSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  val newGeographicalMarketModel = new NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val emptyNewGeographicalMarketModel = new NewGeographicalMarketModel("")
  val whatWilUseForModel = new WhatWillUseForModel("Tobacco growing")
  val usedInvestmentReasonBeforeModel = new UsedInvestmentReasonBeforeModel(Constants.StandardRadioButtonNoValue)
  val previousBeforeDOFCSModel= new PreviousBeforeDOFCSModel(Constants.StandardRadioButtonNoValue)

  class SetupPage {
    val controller = new NewGeographicalMarketController{
      val keyStoreConnector : KeystoreConnector = mockKeyStoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }


  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = Some(whatWillUseFor), usedInvestmentReasonBefore = null and previousDOFCS = null" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = Some(whatWillUseFor), usedInvestmentReasonBefore = Some(usedInvestmentReasonBefore) and previousDOFCS = null" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(usedInvestmentReasonBeforeModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = Some(whatWillUseFor), usedInvestmentReasonBefore = Some(usedInvestmentReasonBefore) " +
    "and previousDOFCS = Some(previousBeforeDOFCS)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(usedInvestmentReasonBeforeModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = None, usedInvestmentReasonBefore = Some(usedInvestmentReasonBefore) " +
    "and previousDOFCS = None" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(usedInvestmentReasonBeforeModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = None, usedInvestmentReasonBefore = None " +
    "and previousDOFCS = Some(previousBeforeDOFCS)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = Some(whatWIllUseFor), usedInvestmentReasonBefore = None " +
    "and previousDOFCS = Some(previousBeforeDOFCS)" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(whatWilUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded " +
    "When whatWillUseFor = None, usedInvestmentReasonBefore = None " +
    "and previousDOFCS = None" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
        "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      )))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewGeographicalMarket page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyNewGeographicalMarketModel)))
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
  }


}
