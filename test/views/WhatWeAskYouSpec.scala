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

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.WhatWeAskYouController
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewTestSpec

class WhatWeAskYouSpec extends ViewTestSpec {

  object TestController extends WhatWeAskYouController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "The What we'll ask you page" should {

    "Verify that the What we'll ask you page contains the correct elements" in new Setup {
      val document: Document = {
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.introduction.WhatWeAskYou.title")
      document.body.getElementById("heading-one").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.one")
      document.body.getElementById("heading-two").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.two")
      document.body.getElementById("heading-three").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.three")
      document.body.getElementById("heading-four").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.four")
      document.body.getElementById("heading-five").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.five")
      document.body.getElementById("description-one").text() shouldBe Messages("page.introduction.WhatWeAskYou.description.one")
      document.body.getElementById("description-two").text() shouldBe Messages("page.introduction.WhatWeAskYou.description.two")
      document.body.getElementById("comp-incorporated").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.compIncorporated")
      document.body.getElementById("comp-do").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.compDo")
      document.body.getElementById("sold-commercially").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.soldCommercially")
      document.body.getElementById("owns-controls-comp").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.ownsControlsComp")
      document.body.getElementById("spent-r-and-d").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.spentRAndD")
      document.body.getElementById("prev-tax-relief").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.prevTaxRelief")
      document.body.getElementById("invest-raised").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.investRaised")
      document.body.getElementById("shares-issued").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.sharesIssued")
      document.body.getElementById("name-schemes").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.nameSchemes")
      document.body.getElementById("how-use-invest").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.howUseInvest")
      document.body.getElementById("amount-to-raise").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.amountToRaise")
      document.body.getElementById("who-to-contact").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.whoToContact")
      document.body.getElementById("contact-details").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.contactDetails")
      document.body.getElementById("company-accounts").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.companyAccounts")
      document.body.getElementById("other-comp-accounts").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.otherCompAccounts")
      document.body.getElementById("memorandum").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.memorandum")
      document.body.getElementById("prospectuses").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.prospectuses")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.QualifyingForSchemeController.show().url
      document.body.getElementById("print-this-page").text() shouldBe Messages("page.introduction.WhatWeAskYou.print.text")
      document.body.getElementById("what-we-ask-you-legend-id").hasClass("visuallyhidden")
      document.body.getElementById("next").text() shouldBe Messages("common.button.continueFirstSection")
    }
  }
}
