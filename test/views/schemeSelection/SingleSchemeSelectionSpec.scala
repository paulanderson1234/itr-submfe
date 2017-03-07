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

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.schemeSelection.SingleSchemeSelection
import forms.schemeSelection.SingleSchemeSelectionForm._
import models.submission.SingleSchemeTypesModel

import scala.concurrent.Future

class SingleSchemeSelectionSpec extends ViewSpec {

  "The SingleSchemeSelection page" should {

    "display the correct elements" in {
      val page = SingleSchemeSelection(singleSchemeSelectionForm)(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.title() shouldBe Messages("page.schemeSelection.SchemeSelection.singlescheme.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.schemeSelection.SchemeSelection.singlescheme.heading")
      document.select("#singleSchemeSelection-eis").size() shouldBe 1
      document.getElementById("singleSchemeSelection-eisLabel").text() shouldBe Messages("page.schemeSelection.SchemeSelection.radiobutton.eis")
      document.getElementById("EIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.subHeading")
      document.getElementById("EIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.text")
      document.getElementById("EIS-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.one")
      document.getElementById("EIS-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.eis.bullet.two")

      document.select("#singleSchemeSelection-seis").size() shouldBe 1
      document.getElementById("singleSchemeSelection-seisLabel").text() shouldBe Messages("page.schemeSelection.SchemeSelection.radiobutton.seis")
      document.getElementById("SEIS-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.subHeading")
      document.getElementById("SEIS-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.seis.text")

      document.select("#singleSchemeSelection-vct").size() shouldBe 1
      document.getElementById("singleSchemeSelection-vctLabel").text() shouldBe Messages("page.schemeSelection.SchemeSelection.radiobutton.vct")
      document.getElementById("VCT-help").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.subHeading")
      document.getElementById("VCT-help-text").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.text")
      document.getElementById("VCT-bullet-one").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.one")
      document.getElementById("VCT-bullet-two").text() shouldBe Messages("page.schemeSelection.SchemeSelection.help.vct.bullet.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

    }
  }

}
