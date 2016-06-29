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
import controllers.IntroductionController
import controllers.examples.routes
import controllers.helpers.FakeRequestHelper
import models.ContactDetailsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class startSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  class SetupPage {

    val controller = new IntroductionController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Start page" should {

    "Verify that start page contains the correct elements" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldEqual Messages("page.start.welcome.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.start.welcome.heading")
      document.body.getElementById("description-one").text() shouldEqual Messages("page.start.welcome.description.one")
      document.body.getElementById("description-two").text() shouldEqual Messages("page.start.welcome.description.two")
      document.body.getElementById("learn").text() shouldEqual Messages("page.start.welcome.bullet.learn")
      document.body.getElementById("eligible").text() shouldEqual Messages("page.start.welcome.bullet.eligible")
      document.body.getElementById("certify").text() shouldEqual Messages("page.start.welcome.bullet.certify")
      document.body.getElementById("claim")text() shouldEqual Messages("page.start.welcome.bullet.claim")
      document.body.getElementById("next").text() shouldEqual Messages("common.button.start")
      document.body.getElementById("read-more-heading").text() shouldEqual Messages("common.readMore")
      document.body.getElementById("how-to-apply").text() shouldEqual Messages("page.start.welcome.apply.link")

    }
  }
}
