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

///*
// * Copyright 2017 HM Revenue & Customs
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
//package views.seis
//
//import auth.MockAuthConnector
//import config.FrontendAppConfig
//import controllers.seis.{TradeStartDateErrorController, routes}
//import org.jsoup.Jsoup
//import org.jsoup.nodes.Document
//import play.api.i18n.Messages
//import play.api.test.Helpers._
//import views.helpers.ViewSpec
//
//class TradeStartDateErrorSpec extends ViewSpec {
//
//  object TestController extends TradeStartDateErrorController {
//    override lazy val applicationConfig = FrontendAppConfig
//    override lazy val authConnector = MockAuthConnector
//    override lazy val enrolmentConnector = mockEnrolmentConnector
//  }
//
//  "The Trading for too long error page" should {
//
//    "Verify that start page contains the correct elements" in new Setup {
//      val document: Document = {
//        val result = TestController.show.apply(authorisedFakeRequest)
//        Jsoup.parse(contentAsString(result))
//      }
//      document.title shouldEqual Messages("page.seis.companyDetails.tradeStartDateError.title")
//      document.body.getElementById("main-heading").text() shouldEqual Messages("page.seis.companyDetails.tradeStartDateError.heading")
//      document.body.getElementById("trading-too-long-reason").text() shouldEqual Messages("page.seis.companyDetails.tradeStartDateError.reason")
//      document.body.getElementById("trading-too-long").text() shouldEqual Messages("page.seis.companyDetails.tradeStartDateError.bullet.one")
//      document.body.getElementById("not-new-business").text() shouldEqual Messages("page.seis.companyDetails.tradeStartDateError.bullet.two")
//      document.body.getElementById("link-text-one").attr("href") shouldEqual "https://www.gov.uk/hmrc-internal-manuals/venture-capital-schemes-manual/8154"
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.TradeStartDateController.show().url
//
//    }
//  }
//}
