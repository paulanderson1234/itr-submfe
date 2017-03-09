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

import common.KeystoreKeys
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eisseis.investment.AnnualTurnoverError

import scala.concurrent.Future

class AnnualTurnoverErrorSpec extends ViewSpec {

  "The Annual turnover error page" should {

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))

      lazy val page = AnnualTurnoverError()(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      "have the title '' " in {
        document.title() shouldBe Messages("page.eisseis.investment.AnnualTurnoverError.title")
      }

      "have the heading '' " in {
        document.getElementById("main-heading").text() shouldBe Messages("page.eisseis.investment.AnnualTurnoverError.heading")
      }

      "have the description '' " in {
        document.getElementById("description").text() shouldBe Messages("page.eisseis.investment.AnnualTurnoverError.description")
      }

      "have the change link '' " in {
        document.getElementById("change-answers").text() shouldBe Messages("page.eisseis.investment.AnnualTurnoverError.incorrect.info") +
          " " + Messages("page.eisseis.investment.AnnualTurnoverError.change.link") + "."

        document.getElementById("change-answers-link").text() shouldBe Messages("page.eisseis.investment.AnnualTurnoverError.change.link")

        document.getElementById("change-answers-link").attr("href") shouldBe controllers.eisseis.routes.TurnoverCostsController.show().toString
      }
  }
}
