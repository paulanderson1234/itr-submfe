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
import config.FrontendAppConfig
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.hmrc.play.http._
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.Play.current

import scala.concurrent.Future

object FileUploadConnector extends FileUploadConnector {
  override lazy val serviceURL = FrontendAppConfig.fileUploadUrl
}

trait FileUploadConnector {

  val serviceURL: String

  // $COVERAGE-OFF$
  def addFileContent(envelopeId: String, fileId: Int, content: File, typeOfContent: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val multipartFormData = MultipartFormData(Map(),Seq(FilePart("attachment", content.getName, Some(typeOfContent), content)),Seq(),Seq())
    WS.url(s"$serviceURL/file-upload/upload/envelopes/$envelopeId/files/$fileId")
      .withHeaders(hc.copy(otherHeaders = Seq("CSRF-token" -> "nocheck")).headers:_*).post(multipartFormData)
  }

}
