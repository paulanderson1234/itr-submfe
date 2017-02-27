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

package views.eisseis

import controllers.eisseis.routes
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.eisseis.previousInvestment.InvalidPreviousScheme

class InvalidPreviousSchemeSpec extends ViewSpec {

  "The Invalid Previous Scheme error page" should {

    "Verify that the page shows both schemes in the bullet list when eis and vct are true" in {
      val page = InvalidPreviousScheme(SchemeTypesModel(eis = true, seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.heading")

      document.body.getElementById("invalid-scheme-reason").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.reason")

      document.body.getElementById("invalid-scheme-next").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.heading")
      document.body.getElementById("invalid-scheme-next-text").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.text")
      document.body.getElementById("valid-schemes").children().size() shouldEqual 2
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.eis")
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.vct")

      document.body.getElementById("continue").text() shouldEqual Messages("common.button.continue")
      document.body.getElementById("continue").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url

      document.body.getElementById("change-answers").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-text") +
        " " + Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-link") + "."
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("change-answers-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
    }

    "Verify that the page shows just eis in the bullet list when eis is true and vct is false" in {
      val page = InvalidPreviousScheme(SchemeTypesModel(eis = true, seis = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.heading")

      document.body.getElementById("invalid-scheme-reason").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.reason")

      document.body.getElementById("invalid-scheme-next").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.heading")
      document.body.getElementById("invalid-scheme-next-text").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.text")
      document.body.getElementById("valid-schemes").children().size() shouldEqual 1
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.eis")

      document.body.getElementById("continue").text() shouldEqual Messages("common.button.continue")
      document.body.getElementById("continue").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url

      document.body.getElementById("change-answers").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-text") +
        " " + Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-link") + "."
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("change-answers-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
    }

    "Verify that the page shows just vct in the bullet list when eis is false and vct is true" in {
      val page = InvalidPreviousScheme(SchemeTypesModel(seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.heading")

      document.body.getElementById("invalid-scheme-reason").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.reason")

      document.body.getElementById("invalid-scheme-next").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.heading")
      document.body.getElementById("invalid-scheme-next-text").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.text")
      document.body.getElementById("valid-schemes").children().size() shouldEqual 1
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.next.vct")

      document.body.getElementById("continue").text() shouldEqual Messages("common.button.continue")
      document.body.getElementById("continue").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url

      document.body.getElementById("change-answers").text() shouldEqual Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-text") +
        " " + Messages("page.eisseis.previousInvestment.InvalidPreviousScheme.change-link") + "."
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("change-answers-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
    }
  }
}
