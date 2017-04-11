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

package views.historicSubmissions

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.historicSubmissions.ApplicationsAA


class ApplicationsAASpec extends ViewSpec {


  "The AA applications page" should {

    "contain the correct elements when loaded" in {

      lazy val page = ApplicationsAA()(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))
      document.title() shouldBe Messages("page.historicSubmissions.applications.aa.title")
      document.body.getElementById("aa-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.advance.assurance.heading")
      document.body.getElementById("aa-application-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.new.heading")
      document.body.getElementById("aa-application-text").text() shouldBe Messages("page.historicSubmissions.applications.aa.new.text")
      document.body.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
      document.body.getElementById("aa-previous-application-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.previous.submitted.applications.heading")
      document.body.getElementById("scheme-table-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.table.schemes.heading")
      document.body.getElementById("date-table-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.table.submissionDate.heading")
      document.body.getElementById("refNo-table-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.table.refNo.heading")
      document.body.getElementById("status-table-heading").text() shouldBe Messages("page.historicSubmissions.applications.aa.table.status.heading")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
    }
  }

}
