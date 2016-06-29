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

import connectors.KeystoreConnector
import controllers.HowToApplyController
import controllers.routes
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class HowToApplySpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{





  class SetupPage {

    val controller = new HowToApplyController{
    }
  }


  "The How to apply page" should {

    "Verify that the How to apply page contains the correct elements" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.introduction.HowToApply.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.introduction.HowToApply.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HowToApplyController.show.toString()
      document.getElementById("number-fill-out").text() should include (Messages("page.introduction.HowToApply.number.fillOut"))
      document.getElementById("number-send").text() should include (Messages("page.introduction.HowToApply.number.send"))
      document.getElementById("number-receive").text() should include (Messages("page.introduction.HowToApply.number.receive"))
      document.getElementById("fill-out-description").text() shouldBe (Messages("page.introduction.HowToApply.fillOut.description"))
      document.getElementById("send-description-one").text() shouldBe (Messages("page.introduction.HowToApply.send.descriptionOne"))
      document.getElementById("send-description-two").text() shouldBe (Messages("page.introduction.HowToApply.send.descriptionTwo"))
      document.getElementById("number-send-one").text() should include (Messages("page.introduction.HowToApply.number.sendOne"))
      document.getElementById("number-send-two").text() shouldBe (Messages("page.introduction.HowToApply.number.sendTwo"))
      document.getElementById("number-send-three").text() shouldBe (Messages("page.introduction.HowToApply.number.sendThree"))
      document.getElementById("receive-description").text() shouldBe (Messages("page.introduction.HowToApply.receive.description"))
      document.getElementById("not-binding").text() shouldBe (Messages("page.introduction.HowToApply.notBinding"))

    }
  }
}
