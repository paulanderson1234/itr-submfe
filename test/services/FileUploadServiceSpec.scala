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

package services

import auth.{TAVCUser, ggUser}
import common.KeystoreKeys
import connectors.{AttachmentsConnector, AttachmentsFrontEndConnector, S4LConnector}
import controllers.helpers.FakeRequestHelper
import data.SubscriptionTestData._
import models.fileUpload.{Envelope, EnvelopeFile, Metadata}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.http.logging.SessionId


class FileUploadServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  object FileUploadServiceSpec extends FileUploadService with FrontendController with FakeRequestHelper with ServicesConfig{
    override val attachmentsFrontEndConnector = mock[AttachmentsFrontEndConnector]
    override val attachmentsConnector: AttachmentsConnector = mock[AttachmentsConnector]
  }

  val mockS4LConnector = mock[S4LConnector]
  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  val envelopeId: Option[String] = Some("000000000000000000000000")
  val envelopeIdStr = envelopeId.get
  val envelopeStatus = "OPEN"
  val files = Seq(EnvelopeFile("1","PROCESSING","test.pdf","application/pdf","2016-03-31T12:33:45Z",Metadata(None),"test.url"))
  val envelopeWithFiles = Envelope(envelopeId.get, envelopeStatus, Some(files))
  val envelopeWithNoFiles = Envelope(envelopeId.get, envelopeStatus, None)

  val envelopeStatusResponse = Json.parse(s"""{
                                             |  "id": "$envelopeIdStr",
                                             |  "callbackUrl": "test",
                                             |  "metadata": {
                                             |  },
                                             |  "status": "$envelopeStatus"
                                             |}""".stripMargin)

  val envelopeStatusWithFileResponse = Json.parse(s"""{
                                                     |  "id": "$envelopeIdStr",
                                                     |  "callbackUrl": "test",
                                                     |  "metadata": {
                                                     |  },
                                                     |  "files": [{
                                                     |   "id": "1",
                                                     |   "status": "PROCESSING",
                                                     |   "name": "test.pdf",
                                                     |   "contentType": "application/pdf",
                                                     |   "length": 5242880,
                                                     |   "created": "2016-03-31T12:33:45Z",
                                                     |   "metadata": {
                                                     |   },
                                                     |   "href": "test.url"
                                                     |  }],
                                                     |  "status": "$envelopeStatus"
                                                     |}""".stripMargin)

  "Calling closeEnvelope" when {

    lazy val result = FileUploadServiceSpec.closeEnvelope(envelopeId.get, validTavcReference)
    lazy val response = await(result)

     "return the response code if any response code is received" in {
       when(FileUploadServiceSpec.attachmentsFrontEndConnector.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),Matchers.any())).
         thenReturn(Future(HttpResponse(CREATED)))
        response.status shouldBe CREATED
      }
    }

  "Calling checkEnvelopeStatus" when {

    "getEnvelopeID is valid and getEnvelopeStatus have files enclosed" should {

      lazy val result = FileUploadServiceSpec.checkEnvelopeStatus(envelopeId.get)

      "Return the envelope with files" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(envelopeId))
        when(FileUploadServiceSpec.attachmentsConnector.getEnvelopeStatus(Matchers.eq(envelopeId.get))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(envelopeStatusWithFileResponse))))

        await(result) shouldBe Some(envelopeWithFiles)
      }
    }

    "getEnvelopeID is valid and getEnvelopeStatus have no files enclosed" should {

      lazy val result = FileUploadServiceSpec.checkEnvelopeStatus(envelopeId.get)

      "Return the envelope with no files" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(envelopeId))
        when(FileUploadServiceSpec.attachmentsConnector.getEnvelopeStatus(Matchers.eq(envelopeId.get))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(envelopeStatusResponse))))

        await(result) shouldBe Some(envelopeWithNoFiles)
      }
    }
  }

  "Calling getEnvelopeFiles" when {

    "checkEnvelopeStatus is processed with files enclosed" should {

      lazy val result = FileUploadServiceSpec.getEnvelopeFiles(envelopeId.get)

      "Return the envelope with files" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(envelopeId))
        when(FileUploadServiceSpec.attachmentsConnector.getEnvelopeStatus(Matchers.eq(envelopeId.get))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(envelopeStatusWithFileResponse))))

        assert(await(result).nonEmpty)

        result.flatMap {
          files => files.map{
                file => {
                  assertResult(file.status)("PROCESSING")
                  assertResult(file.id)("1")
                }
              }
            }

      }
    }

    "checkEnvelopeStatus is processed with no files enclosed" should {

      lazy val result = FileUploadServiceSpec.getEnvelopeFiles(envelopeId.get)

      "Return the envelope with no files" in {

        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(envelopeId))
        when(FileUploadServiceSpec.attachmentsConnector.getEnvelopeStatus(Matchers.eq(envelopeId.get))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(envelopeStatusResponse))))

        assert(await(result).isEmpty)
      }
    }
  }
}
