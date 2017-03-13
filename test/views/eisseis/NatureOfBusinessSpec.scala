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

import auth.{MockConfig, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.NatureOfBusinessController
import controllers.routes
import models.NatureOfBusinessModel
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class NatureOfBusinessSpec extends ViewSpec {

  object TestController extends NatureOfBusinessController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }


  def setupMocks(natureOfBusinessModel: Option[NatureOfBusinessModel] = None): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "The Nature of business page" should {

    "Verify that the page contains the correct elements when a valid NatureOfBusinessModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(natureOfBusinessModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.natureofbusiness.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-natureofbusiness").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness-hint").text() shouldBe Messages("page.companyDetails.natureofbusiness.question.hint")
      document.getElementById("description-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    }

    "Verify that the nature of business page contains the correct elements when an invalid NatureOfBusinessModel model is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      // Check the error summary is displayed - the whole purpose of this test
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      // additional page checks to make sure everything else still as expected if errors on page
      document.title() shouldBe Messages("page.companyDetails.natureofbusiness.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-natureofbusiness").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness-hint").text() shouldBe Messages("page.companyDetails.natureofbusiness.question.hint")
      document.getElementById("description-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    }

  }

}
