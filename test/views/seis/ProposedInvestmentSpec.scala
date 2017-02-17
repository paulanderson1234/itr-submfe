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

import controllers.seis.routes
import forms.ProposedInvestmentForm._
import models.ProposedInvestmentModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.investment.ProposedInvestment

class ProposedInvestmentSpec extends ViewSpec {

  val page = (form: Form[ProposedInvestmentModel]) =>
    ProposedInvestment(form, routes.ProposedInvestmentController.show().url)(fakeRequest, applicationMessages)

  "The Proposed Investment page" should {

    "Verify that the proposed investment page contains the correct elements when a valid ProposedInvestmentModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(proposedInvestmentForm.fill(ProposedInvestmentModel(2))).body)
      document.title() shouldBe Messages("page.seis.investment.proposedInvestment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.investment.proposedInvestment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.seis.investment.proposedInvestment.amount.heading")
      document.getElementById("help").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.two")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ProposedInvestmentModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(proposedInvestmentForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.seis.investment.proposedInvestment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.investment.proposedInvestment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.seis.investment.proposedInvestment.amount.heading")
      document.getElementById("help").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.two")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the proposed investment page contains the correct elements when an empty ProposedInvestmentModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(proposedInvestmentForm).body)
      document.title() shouldBe Messages("page.seis.investment.proposedInvestment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.seis.investment.proposedInvestment.amount.heading")
      document.getElementById("help").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.link.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.two")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.seis.investment.proposedInvestment.help.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }
  }

}
