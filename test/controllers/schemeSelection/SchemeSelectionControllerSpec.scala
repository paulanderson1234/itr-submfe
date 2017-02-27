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

package controllers.schemeSelection

import akka.stream.Materializer
import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import play.api.test.Helpers._
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.Upstream5xxResponse

import scala.concurrent.Future

class SchemeSelectionControllerSpec extends BaseSpec {

  val schemeSelectionModel = SchemeTypesModel(eis = true, seis = true, vct = true)
  implicit lazy val materializer: Materializer = app.materializer

  object TestController extends SchemeSelectionController {
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
  }

  "SchemeSelectionController" should {

    "Use the correct S4LConnector" in {
      SchemeSelectionController.s4lConnector shouldBe S4LConnector
    }

    "Use the correct EnrolmentConnector" in {
      SchemeSelectionController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "Use the frontend application config" in {
      SchemeSelectionController.applicationConfig shouldBe FrontendAppConfig
    }

    "Use the FrontendAuthConnector" in {
      SchemeSelectionController.authConnector shouldBe FrontendAuthConnector
    }

  }

  "Sending an Authenticated and Enrolled GET request with a session to SchemeSelectionController" when {

    "s4lConnector retrieves a SchemeSelectionModel" should {

      "pass a form to the page with the stored data" in {
        mockEnrolledRequest()
        when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeSelectionModel)))
        showWithSessionAndAuth(TestController.show()) {
          result =>
            val document = Jsoup.parse(bodyOf(result))
            document.getElementById("EIS").attr("checked") shouldBe "checked"
            document.getElementById("SEIS").attr("checked") shouldBe "checked"
            document.getElementById("VCT").attr("checked") shouldBe "checked"
            status(result) shouldBe OK
        }
      }

    }

    "s4lConnector does not retrieve a SchemeSelectionModel" should {

      "pass an empty form to the page" in {
        mockEnrolledRequest()
        when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        showWithSessionAndAuth(TestController.show()) {
          result =>
            val document = Jsoup.parse(bodyOf(result))
            document.getElementById("EIS").attr("checked") shouldBe ""
            document.getElementById("SEIS").attr("checked") shouldBe ""
            document.getElementById("VCT").attr("checked") shouldBe ""
            status(result) shouldBe OK
        }
      }

    }

  }

  "Sending an authenticated and enrolled POST request with a session to the SchemeSelectionController" when {

    "only EIS is selected in the form" should {

      "redirect the user to the first page of the EIS flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
         "EIS" -> "true",
         "SEIS" -> "false",
         "VCT" -> "false"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "only SEIS is selected in the form" should {

      "redirect the user to the first page of the SEIS flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "false",
          "SEIS" -> "true",
          "VCT" -> "false"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "only VCT is selected in the form" should {

      "redirect the user to the first page of the VCT flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "false",
          "SEIS" -> "false",
          "VCT" -> "true"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "EIS and SEIS are selected in the form" should {

      "redirect the user to the first page of the EIS SEIS combined flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "true",
          "SEIS" -> "true",
          "VCT" -> "false"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eisseis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "EIS and VCT are selected in the form" should {

      "redirect the user to the first page of the EIS VCT combined flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "true",
          "SEIS" -> "false",
          "VCT" -> "true"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "SEIS and VCT are selected in the form" should {

      "redirect the user to the first page of the SEIS VCT combined flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "false",
          "SEIS" -> "true",
          "VCT" -> "true"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eisseis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "EIS, SEIS and VCT are selected in the form" should {

      "redirect the user to the first page of the EIS SEIS VCT combined flow" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "true",
          "SEIS" -> "true",
          "VCT" -> "true"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eisseis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

    "no options are selected in the form" should {

      "return a BAD_REQUEST" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "false",
          "SEIS" -> "false",
          "VCT" -> "false"
        ){
          result =>
            status(result) shouldBe BAD_REQUEST
        }
      }

    }

    "an invalid form is sent" should {

      "return a BAD_REQUEST" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "" -> ""
        ){
          result =>
            status(result) shouldBe BAD_REQUEST
        }
      }

    }

    "saveFormData returns a failed future" should {

      "redirect the user" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.failed(Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit(),
          "EIS" -> "true",
          "SEIS" -> "false",
          "VCT" -> "false"
        ){
          result =>
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      }

    }

  }

}
