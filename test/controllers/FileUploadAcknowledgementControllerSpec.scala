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

package controllers

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import models.fileUpload.{EnvelopeFile, Metadata}
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future


class FileUploadAcknowledgementControllerSpec extends BaseSpec {

  object TestController extends FileUploadAcknowledgementController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
    val fileUploadService: FileUploadService = mockFileUploadService
  }

  def setupMocks(): Unit = {

    val files = Seq(EnvelopeFile("1","PROCESSING","test.pdf","application/pdf","2016-03-31T12:33:45Z",Metadata(None),"test.url"))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
    when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future(HttpResponse(CREATED)))

  }

  "TradingForTooLongController" should {
    "use the correct auth connector" in {
      FileUploadAcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      FileUploadAcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to TradingForTooLongController when authenticated and enrolled" should {
    "return a 200 OK Swhen something is fetched from keystore" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 OK when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }
}
