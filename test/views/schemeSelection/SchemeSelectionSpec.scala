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

package views.schemeSelection

import controllers.helpers.BaseSpec
import controllers.routes
import forms.schemeSelection.SchemeSelectionForm
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.html.schemeSelection.SchemeSelection
import play.api.i18n.Messages.Implicits._
import play.api.data.Form

class SchemeSelectionSpec extends BaseSpec {

  val allTrueForm = SchemeSelectionForm.schemeSelectionForm.fill(SchemeTypesModel(true,true,false,true))
  private def page(form: Form[SchemeTypesModel]) = SchemeSelection(form)(fakeRequest,applicationMessages)

  "Verify that the scheme selection page contains the correct elements " +
    "when a form with all elements set as true is passed to the page" in {
      val document = Jsoup.parse(page(allTrueForm).body)

      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.title() shouldBe Messages("page.schemeSelection.SchemeSelection.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.schemeSelection.SchemeSelection.heading")
      document.getElementById("SEIS").attr("checked") shouldBe "checked"
      document.getElementById("SEIS-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.seis")
      document.getElementById("SEIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.subHeading")
      document.getElementById("SEIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.text")
      document.getElementById("EIS").attr("checked") shouldBe "checked"
      document.getElementById("EIS-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.eis")
      document.getElementById("EIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.subHeading")
      document.getElementById("EIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.text")
      document.getElementById("EIS-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.one")
      document.getElementById("EIS-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.two")
      document.getElementById("VCT").attr("checked") shouldBe "checked"
      document.getElementById("VCT-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.vct")
      document.getElementById("VCT-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.subHeading")
      document.getElementById("VCT-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.text")
      document.getElementById("VCT-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.one")
      document.getElementById("VCT-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that the scheme selection page contains the correct elements " +
    "when an empty form is passed to the page" in {
      val document = Jsoup.parse(page(SchemeSelectionForm.schemeSelectionForm).body)

      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.title() shouldBe Messages("page.schemeSelection.SchemeSelection.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.schemeSelection.SchemeSelection.heading")
      document.getElementById("SEIS").attr("checked") shouldBe ""
      document.getElementById("SEIS-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.seis")
      document.getElementById("SEIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.subHeading")
      document.getElementById("SEIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.text")
      document.getElementById("EIS").attr("checked") shouldBe ""
      document.getElementById("EIS-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.eis")
      document.getElementById("EIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.subHeading")
      document.getElementById("EIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.text")
      document.getElementById("EIS-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.one")
      document.getElementById("EIS-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.two")
      document.getElementById("VCT").attr("checked") shouldBe ""
      document.getElementById("VCT-label").text() shouldBe Messages("page.schemeSelection.SchemeSelection.checkbox.vct")
      document.getElementById("VCT-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.subHeading")
      document.getElementById("VCT-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.text")
      document.getElementById("VCT-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.one")
      document.getElementById("VCT-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

}
