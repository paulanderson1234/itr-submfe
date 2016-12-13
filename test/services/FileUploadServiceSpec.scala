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

package services

import java.nio.file.Files

import auth.{TAVCUser, ggUser}
import common.KeystoreKeys
import connectors.{FileUploadConnector, S4LConnector, SubmissionConnector}
import models.upload.{Envelope, EnvelopeFile, Metadata}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FileUploadServiceSpec extends UnitSpec with MockitoSugar with WithFakeApplication with BeforeAndAfter {

  val mockFileUploadConnector = mock[FileUploadConnector]
  val mockS4LConnector = mock[S4LConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]
  val envelopeID = "00000000-0000-0000-0000-000000000000"
  val fileID = 1
  val envelopeStatus = "OPEN"
  val fileName = "test"
  implicit val hc = HeaderCarrier()
  implicit val user = TAVCUser(ggUser.allowedAuthContext)

  val envelopeStatusResponse = Json.parse(s"""{
  |  "id": "$envelopeID",
  |  "callbackUrl": "test",
  |  "metadata": {
  |  },
  |  "status": "$envelopeStatus"
  |}""".stripMargin)

  val envelopeStatusWithFileResponse = Json.parse(s"""{
  |  "id": "$envelopeID",
  |  "callbackUrl": "test",
  |  "metadata": {
  |  },
  |  "files": [{
  |   "id": "1",
  |   "status": "PROCESSING",
  |   "name": "test.pdf",
  |   "contentType": "application/pdf",
  |   "created": "2016-03-31T12:33:45Z",
  |   "metadata": {
  |   },
  |   "href": "test.url"
  |  }],
  |  "status": "$envelopeStatus"
  |}""".stripMargin)

  val createEnvelopeResponse = Json.parse(s"""{
  | "envelopeID": "$envelopeID"
  |}""".stripMargin)

  val files = Seq(EnvelopeFile("1","PROCESSING","test.pdf","application/pdf","2016-03-31T12:33:45Z",Metadata(None),"test.url"))

  val envelope = Envelope(envelopeID,envelopeStatus,None)
  val envelopeWithFiles = Envelope(envelopeID,envelopeStatus,
    Some(files))

  case class FakeWSResponse(status: Int) extends WSResponse {
    override def allHeaders = ???
    override def statusText = ???
    override def underlying[T] = ???
    override def xml = ???
    override def body = ???
    override def header(key: String) = ???
    override def cookie(name: String) = ???
    override def cookies = ???
    override def json = ???
  }

  before{
    reset(mockFileUploadConnector)
  }

  object TestService extends FileUploadService {
    override lazy val fileUploadConnector = mockFileUploadConnector
    override lazy val s4lConnector = mockS4LConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "FileUploadService" should {

    "Use the correct FileUploadConnector" in {
      FileUploadService.fileUploadConnector shouldBe FileUploadConnector
    }

  }

  "getEnvelopeID" when {

    "createNewID is true and envelopeID is in save4later" should {

      lazy val result = TestService.getEnvelopeID()

      "return an envelopeID if created successfully" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        await(result) shouldBe envelopeID
      }

    }

    "createNewID is true and envelopeID is not in save4later" should {

      lazy val result = TestService.getEnvelopeID()

      "return an envelopeID if created successfully" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockSubmissionConnector.createEnvelope()(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(createEnvelopeResponse))))
        await(result) shouldBe envelopeID
      }

    }

    "createNewID is true and envelopeID is an empty string in save4later" should {

      lazy val result = TestService.getEnvelopeID()

      "return an envelopeID if created successfully" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some("")))
        when(mockSubmissionConnector.createEnvelope()(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(createEnvelopeResponse))))
        await(result) shouldBe envelopeID
      }

    }

    "createNewID is false and envelopeID is not in save4later" should {

      lazy val result = TestService.getEnvelopeID(createNewID = false)

      "return an envelopeID if created successfully" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        await(result) shouldBe ""
      }

    }

  }

  "checkEnvelopeStatus" when {

    "getEnvelopeID returns a non empty string and getEnvelopeStatus returns OK" should {

      lazy val result = TestService.checkEnvelopeStatus

      "Return the envelope" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusResponse))))
        await(result) shouldBe Some(envelope)
      }

    }

    "getEnvelopeID returns a non empty string and getEnvelopeStatus returns non OK" should {

      lazy val result = TestService.checkEnvelopeStatus

      "Return None" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
        await(result) shouldBe None
      }

    }

  }

  "uploadFile" when {

    val testFile = Array("1".toByte)

    "The envelope has no files and the file is uploaded successfully" should {

      lazy val result = TestService.uploadFile(testFile, fileName, envelopeID)

      "Return OK" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusResponse))))
        when(mockFileUploadConnector.addFileContent(Matchers.eq(envelopeID), Matchers.eq(1), Matchers.eq(fileName),
          Matchers.eq(testFile), Matchers.eq(TestService.PDF))(Matchers.any())).thenReturn(Future.successful(FakeWSResponse(OK)))
        await(result).status shouldBe OK
      }

    }

    "The envelope has a file and the file is uploaded successfully" should {

      lazy val result = TestService.uploadFile(testFile, fileName, envelopeID)

      "Return OK" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusWithFileResponse))))
        when(mockFileUploadConnector.addFileContent(Matchers.eq(envelopeID), Matchers.eq(2), Matchers.eq(fileName),
          Matchers.eq(testFile), Matchers.eq(TestService.PDF))(Matchers.any())).thenReturn(Future.successful(FakeWSResponse(OK)))
        await(result).status shouldBe OK
      }

    }

    "The file is not uploaded successfully" should {

      lazy val result = TestService.uploadFile(testFile, fileName, envelopeID)

      "Return OK" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusWithFileResponse))))
        when(mockFileUploadConnector.addFileContent(Matchers.eq(envelopeID), Matchers.eq(2), Matchers.eq(fileName),
          Matchers.eq(testFile), Matchers.eq(TestService.PDF))(Matchers.any())).thenReturn(Future.successful(FakeWSResponse(INTERNAL_SERVER_ERROR)))
        await(result).status shouldBe INTERNAL_SERVER_ERROR
      }

    }

  }

  "getEnvelopeFiles" when {

    "checkEnvelopeStatus returns an envelope with a file" should {

      lazy val result = TestService.getEnvelopeFiles

      "return a sequence with envelope files in it" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusWithFileResponse))))
        await(result) shouldBe files
      }

    }

    "checkEnvelopeStatus returns an envelope with no files" should {

      lazy val result = TestService.getEnvelopeFiles

      "return an empty Seq" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.getEnvelopeStatus(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK,Some(envelopeStatusResponse))))
        await(result) shouldBe Seq()
      }

    }

  }

  "closeEnvelope" when {

    "getEnvelopeID returns a non-empty envelope ID, and closeEnvelope returns OK" should {

      lazy val result = TestService.closeEnvelope

      "return the http response" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.closeEnvelope(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK)))
        await(result).status shouldBe OK
      }

    }

    "getEnvelopeID returns a non-empty envelope ID, and closeEnvelope returns non OK" should {

      lazy val result = TestService.closeEnvelope

      "return the http response" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(envelopeID)))
        when(mockSubmissionConnector.closeEnvelope(Matchers.eq(envelopeID))(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
        await(result).status shouldBe INTERNAL_SERVER_ERROR
      }

    }

    "getEnvelopeID returns an empty envelope ID" should {

      lazy val result = TestService.closeEnvelope

      "return an INTERNAL_SERVER_ERROR" in {
        when(mockS4LConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeID))(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some("")))
        await(result).status shouldBe OK
      }

    }

  }

}
