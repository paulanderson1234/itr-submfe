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

import builders.SessionBuilder
import connectors.KeystoreConnector
import controllers.{QualifyingForSchemeController, routes}
import controllers.helpers.{FakeRequestHelper, TestHelper}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class QualifyingForSchemeSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  class SetupPage {

    val controller = new QualifyingForSchemeController{}
  }

  "The Qualifying for Scheme page" should {

    "Verify that start page contains the correct elements" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldEqual Messages("page.introduction.qualifyingForScheme.title")
      document.body.getElementById("heading-one").text() shouldEqual Messages("page.introduction.qualifyingForScheme.heading.one")
      document.body.getElementById("description-one").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.one")
      document.body.getElementById("description-two").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.two")
      document.body.getElementById("description-three").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.three") + " " +
        TestHelper.getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.schemeGuidance"))
      document.body.getElementById("description-four").text() shouldEqual Messages("page.introduction.qualifyingForScheme.description.four")
      document.body.getElementById("description-five").text() shouldEqual (Messages("page.introduction.qualifyingForScheme.description.five") + " " +
        TestHelper.getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.unqualifiedBusiness")))
      document.body.getElementById("uk-base").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.UKbase")
      document.body.getElementById("listed").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.listed")
      document.body.getElementById("num-employees").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.numEmployees")
      document.body.getElementById("farming").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.farming")
      document.body.getElementById("legal").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.legal")
      document.body.getElementById("property").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.property")
      document.body.getElementById("hotels").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.hotels")
      document.body.getElementById("electricity").text() shouldEqual Messages("page.introduction.qualifyingForScheme.bullet.electricity")
      document.body.getElementById("link-text-one").text() shouldEqual TestHelper.getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.schemeGuidance"))
      document.body.getElementById("link-text-two").text() shouldEqual TestHelper.getExternalLinkText(Messages("page.introduction.qualifyingForScheme.link.unqualifiedBusiness"))
      document.body.getElementById("link-text-one").attr("href") shouldEqual "https://www.gov.uk/government/publications/the-enterprise-investment-scheme-introduction"
      document.body.getElementById("link-text-two").attr("href") shouldEqual "https://www.gov.uk/hmrc-internal-manuals/venture-capital-schemes-manual/vcm3000"
      document.body.getElementById("back-link").attr("href") shouldEqual routes.YourCompanyNeedController.show.toString()
      document.body.getElementById("continue").text() shouldEqual Messages("common.button.continue")
    }
  }
}
