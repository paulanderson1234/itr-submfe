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

package connectors

import java.io.File

import utils.MultipartFormDataWriteable._
import config.{FrontendAppConfig, WSHttp}
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.hmrc.play.http._
import play.api.Play.current
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.filters.csrf.CSRF

import scala.concurrent.Future

object FileUploadConnector extends FileUploadConnector {
  override lazy val http = WSHttp
  override lazy val serviceURL = FrontendAppConfig.fileUploadUrl
}

trait FileUploadConnector {

  val http: HttpGet with HttpPost with HttpPut with HttpDelete
  val serviceURL: String

  val baseURL = "http://localhost:8898"

  def createEnvelope()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val createEnvelopeJSON = Json.parse("""{
      |  "constraints" : {
      |    "contentTypes" : [
      |        "application/pdf"
      |    ],
      |    "maxItems" : 5,
      |    "maxSize" : "25MB",
      |    "maxSizePerItem" : "5MB"
      |  }
      |}""".stripMargin)

    http.POST(s"$baseURL/file-upload/envelopes", createEnvelopeJSON)
  }

  def getEnvelopeStatus(envelopeId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET(s"$baseURL/file-upload/envelopes/$envelopeId")
  }

  def closeEnvelope(envelopeId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val closeEnvelopeJSON = Json.parse(s"""{
      |   "envelopeId" : "$envelopeId",
      |   "application" : "tavc",
      |   "destination" : "DMS"
      |}""".stripMargin)

    http.POST(s"$baseURL/file-routing/requests", closeEnvelopeJSON)
  }

  def addFileContent(envelopeId: String, fileId: Int, content: File, typeOfContent: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val multipartFormData = MultipartFormData(Map(),Seq(FilePart("attachment", content.getName, Some(typeOfContent), content)),Seq(),Seq())
    WS.url(s"$serviceURL/file-upload/upload/envelopes/$envelopeId/files/$fileId")
      .withHeaders(hc.copy(otherHeaders = Seq("CSRF-token" -> "nocheck")).headers:_*).post(multipartFormData)
  }

  def addFileMetadata(envelopeId: String, fileId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    // TODO: Agreed metadata goes here
    val addFileMetadataJSON = Json.parse("""{
      |   "metadata" : {
      |   }
      |}""".stripMargin)

    http.PUT(s"$baseURL/file-upload/envelopes/$envelopeId/files/$fileId/metadata", addFileMetadataJSON)
  }

  def deleteFile(envelopeId: String, fileId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE(s"$baseURL/file-upload/envelopes/$envelopeId/files/$fileId")
  }


}
