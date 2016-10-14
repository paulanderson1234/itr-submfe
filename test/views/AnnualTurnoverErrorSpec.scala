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

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.investment.AnnualTurnoverError


class AnnualTurnoverErrorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  "The Annual turnover error page" should {

      lazy val page = AnnualTurnoverError()(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      "have the title '' " in {
        document.title() shouldBe Messages("page.investment.AnnualTurnoverError.title")
      }

      "have the heading '' " in {
        document.getElementById("main-heading").text() shouldBe Messages("page.investment.AnnualTurnoverError.heading")
      }

      "have the description '' " in {
        document.getElementById("description").text() shouldBe Messages("page.investment.AnnualTurnoverError.description")
      }

      "have the link text '' " in {
        document.getElementById("guidance-link").text() shouldBe Messages("page.investment.AnnualTurnoverError.guidance")
      }
  }
}
