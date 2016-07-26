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

///*
// * Copyright 2016 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
///**
//  * Copyright 2016 HM Revenue & Customs
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//
//package views
//
//import java.util.UUID
//
//import builders.SessionBuilder
//import connectors.KeystoreConnector
//import controllers.{SubsidiariesSpendingInvestmentController, routes}
//import controllers.helpers.FakeRequestHelper
//import models.SubsidiariesSpendingInvestmentModel
//import org.jsoup.Jsoup
//import org.scalatest.mock.MockitoSugar
//import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
//import org.jsoup.nodes.Document
//import org.mockito.Matchers
//import org.mockito.Mockito._
//import play.api.i18n.Messages
//import play.api.test.Helpers._
//
//import scala.concurrent.Future
//
//class SubsidiariesSpendingInvestmentSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {
//
//  val mockKeyStoreConnector = mock[KeystoreConnector]
//
//  val subsidiariesSpendingInvestmentModel = new SubsidiariesSpendingInvestmentModel("Yes")
//  val emptySubsidiariesSpendingInvestmentModel = new SubsidiariesSpendingInvestmentModel("")
//
//  class SetupPage {
//    val controller = new SubsidiariesSpendingInvestmentController{
//      val keyStoreConnector : KeystoreConnector = mockKeyStoreConnector
//    }
//  }
//
//  "The SubsidiariesSpendingInvestment Page" should {
//
//    "Verify that the correct elements are loaded" in new SetupPage{
//      val document : Document = {
//        val userID = s"user-${UUID.randomUUID}"
//        when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any()))
//          .thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
//        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userID))
//        Jsoup.parse(contentAsString(result))
//      }
//
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString()
//      document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
//      document.select("#subSpendingInvestment-yes").size() shouldBe 1
//      document.select("#subSpendingInvestment-no").size() shouldBe 1
//      document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
//    }
//  }
//
//
//}
