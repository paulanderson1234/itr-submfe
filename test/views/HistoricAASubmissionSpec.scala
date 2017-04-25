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

import controllers.routes
import models.ProposedInvestmentModel
import models.submission.{Scheme, AASubmissionDetailsModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.historicSubmissions.HistoricAASubmission


class HistoricAASubmissionSpec extends ViewSpec {


  "The AA applications page" should {

    val aASubmissionDetailsModelOne = AASubmissionDetailsModel(Some("000000123456"), Some("Compliance Statement"),
      Some("2015-09-22"), Some(List(Scheme(Some("EIS")), Scheme(Some("VCT")))), Some("Received"), Some("003333333333"))
    val aASubmissionDetailsModelTwo = AASubmissionDetailsModel(Some("000000000000"), Some("Advance Assurance"),
      Some("2015-09-22"), Some(List(Scheme(Some("EIS")),Scheme(Some("SEIS")))), Some("Rejected"), Some("003333333334"))


    "contain the correct elements when loaded with a list of historic submission" in {

      lazy val page = HistoricAASubmission(List(aASubmissionDetailsModelOne,aASubmissionDetailsModelTwo))(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))
      lazy val historicSubmissionTable = document.getElementById("table-div").getElementById("previous-schemes-table").select("tbody")
      document.title() shouldBe Messages("page.historicSubmissions.applications.aa.title")
      document.body.getElementById("heading").getElementById("pre-header")text() shouldBe
        Messages("page.historicSubmissions.applications.aa.advance.assurance.heading")
      document.body.getElementById("heading").getElementById("page-header").text() shouldBe
        Messages("page.historicSubmissions.applications.aa.your.applications.heading")
      document.body.getElementById("aa-application-heading").text() shouldBe
        Messages("page.historicSubmissions.applications.aa.new.heading")
      document.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      historicSubmissionTable.select("tr").get(0).getElementById("record-0-schemeName").text() shouldBe
        "Enterprise Investment Scheme and Venture Capital Trust"
      historicSubmissionTable.select("tr").get(0).getElementById("record-0-date").text() shouldBe
        "2015-09-22"
      historicSubmissionTable.select("tr").get(0).getElementById("record-0-formBundle").text() shouldBe
        "000000123456"
      historicSubmissionTable.select("tr").get(0).getElementById("record-0-status").text() shouldBe
        "Received"
      historicSubmissionTable.select("tr").get(1).getElementById("record-1-schemeName").text() shouldBe
      "Enterprise Investment Scheme and Seed Enterprise Investment Scheme"
      historicSubmissionTable.select("tr").get(1).getElementById("record-1-date").text() shouldBe
        "2015-09-22"
      historicSubmissionTable.select("tr").get(1).getElementById("record-1-formBundle").text() shouldBe
        "000000000000"
      historicSubmissionTable.select("tr").get(1).getElementById("record-1-status").text() shouldBe
        "Rejected"


    }

    "contain the correct elements when loaded with a empty list of historic submission" in {

      lazy val page = HistoricAASubmission(List())(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))
      lazy val historicSubmissionTable = document.getElementById("table-div").getElementById("previous-schemes-table").select("tbody")
      document.title() shouldBe Messages("page.historicSubmissions.applications.aa.title")
      document.body.getElementById("heading").getElementById("pre-header")text() shouldBe
        Messages("page.historicSubmissions.applications.aa.advance.assurance.heading")
      document.body.getElementById("heading").getElementById("page-header").text() shouldBe
        Messages("page.historicSubmissions.applications.aa.your.applications.heading")
      document.body.getElementById("aa-application-heading").text() shouldBe
        Messages("page.historicSubmissions.applications.aa.new.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
      intercept[NullPointerException]{
        historicSubmissionTable.select("tr").size()
      }
    }
  }

}