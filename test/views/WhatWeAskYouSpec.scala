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

import controllers.WhatWeAskYouController
import controllers.examples.routes
import controllers.helpers.{FakeRequestHelper}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class WhatWeAskYouSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  class SetupPage {

    val controller = new WhatWeAskYouController{
    }
  }


  "The What we'll ask you page" should {

    "Verify that the What we'll ask you page contains the correct elements" in new SetupPage {
      val document: Document = {
        //val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply(fakeRequestWithSession)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.introduction.WhatWeAskYou.title")
      document.getElementById("heading-one").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.one")
      document.getElementById("heading-two").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.two")
      document.getElementById("heading-three").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.three")
      document.getElementById("heading-four").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.four")
      document.getElementById("heading-five").text() shouldBe Messages("page.introduction.WhatWeAskYou.heading.five")
      document.getElementById("description-one").text() shouldBe Messages("page.introduction.WhatWeAskYou.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.introduction.WhatWeAskYou.description.two")
      document.getElementById("comp-incorporated").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.compIncorporated")
      document.getElementById("comp-do").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.compDo")
      document.getElementById("sold-commercially").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.soldCommercially")
      document.getElementById("owns-controls-comp").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.ownsControlsComp")
      document.getElementById("spent-r-and-d").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.spentRAndD")
      document.getElementById("prev-tax-relief").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.prevTaxRelief")
      document.getElementById("invest-raised").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.investRaised")
      document.getElementById("shares-issued").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.sharesIssued")
      document.getElementById("name-schemes").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.nameSchemes")
      document.getElementById("how-use-invest").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.howUseInvest")
      document.getElementById("amount-to-raise").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.amountToRaise")
      document.getElementById("who-to-contact").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.whoToContact")
      document.getElementById("contact-details").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.contactDetails")
      document.getElementById("company-accounts").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.companyAccounts")
      document.getElementById("other-comp-accounts").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.otherCompAccounts")
      document.getElementById("memorandum").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.memorandum")
      document.getElementById("prospectuses").text() shouldBe Messages("page.introduction.WhatWeAskYou.bullet.prospectuses")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWeAskYouController.show.toString()
      document.getElementById("next").text() shouldBe Messages("common.button.continue")

    }
  }
}
