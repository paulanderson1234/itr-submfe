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
import controllers.{QualifyingForSchemeController, routes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewTestSpec

class QualifyingForSchemeSpec extends ViewTestSpec {

  object TestController extends QualifyingForSchemeController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "The Qualifying for Scheme page" should {

    "Verify that start page contains the correct elements" in new Setup {
      val document: Document = {
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title shouldEqual Messages("page.introduction.qualifyingForScheme.title")
      document.body.getElementById("heading-one").text() shouldEqual Messages("page.introduction.qualifyingForScheme.heading.one")
      document.body.getElementById("description-one").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.one")
      document.body.getElementById("description-two").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.two")
      document.body.getElementById("description-three").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.three") + " " +
        getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.schemeGuidance"))
      document.body.getElementById("description-four").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.four")
      document.body.getElementById("description-five").text() shouldEqual (Messages("page.introduction.qualifyingForScheme.description.five") + " " +
        getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.unqualifiedBusiness")))
      document.body.getElementById("uk-base").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.UKbase")
      document.body.getElementById("listed").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.listed")
      document.body.getElementById("num-employees").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.numEmployees")
      document.body.getElementById("farming").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.farming")
      document.body.getElementById("legal").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.legal")
      document.body.getElementById("property").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.property")
      document.body.getElementById("hotels").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.hotels")
      document.body.getElementById("electricity").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.electricity")
      document.body.getElementById("link-text-one").text() shouldEqual
        getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.schemeGuidance"))
      document.body.getElementById("link-text-two").text() shouldEqual
        getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.unqualifiedBusiness"))
      document.body.getElementById("link-text-one").attr("href") shouldEqual
        "https://www.gov.uk/government/publications/the-enterprise-investment-scheme-introduction"
      document.body.getElementById("link-text-two").attr("href") shouldEqual
        "https://www.gov.uk/hmrc-internal-manuals/venture-capital-schemes-manual/vcm3000"
      document.body.getElementById("back-link").attr("href") shouldEqual routes.YourCompanyNeedController.show().url
      document.body.getElementById("continue").text() shouldEqual Messages("common.button.continue")
    }
  }
}
