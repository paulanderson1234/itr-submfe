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

import controllers.helpers.BaseSpec
import models.PreviousSchemeModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.html.eis.previousInvestment.DeletePreviousScheme

class DeletePreviousSchemeSpec  extends BaseSpec {

  val schemeName = "Enterprise Investment Scheme"
  val shareIssue = "20 May 2015"
  val schemeToDelete = PreviousSchemeModel("EIS",29000,None,None,Some(20),Some(5),Some(2015),Some(1))

  "The previous scheme delete page" should {

    "contain the correct elements when loaded with a share issue and investment name" in {

      lazy val page = DeletePreviousScheme(schemeToDelete)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.deletePreviousScheme.title")

      document.body.getElementById("main-heading").text() shouldBe Messages("page.deletePreviousScheme.heading")
      document.body.getElementById("scheme-delete-hint").text() shouldBe Messages("page.deletePreviousScheme.hint", schemeName, shareIssue)
      document.body.getElementById("scheme-remove").text() shouldBe Messages("page.deletePreviousScheme.confirm")
      document.body.getElementById("scheme-cancel").text() shouldBe Messages("page.deletePreviousScheme.cancel")
    }
  }

}
