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

package utils

import java.io.ByteArrayOutputStream

import com.ning.http.client.FluentCaseInsensitiveStringsMap
import com.ning.http.multipart.{ByteArrayPartSource, FilePart, MultipartRequestEntity, StringPart}
import play.api.http.HeaderNames._
import play.api.http.{ContentTypeOf, Writeable}
import play.api.mvc.{Codec, MultipartFormData}

object MultipartFormDataWriteable {

  implicit def contentType[A](implicit codec: Codec): ContentTypeOf[MultipartFormData[A]] = {
    ContentTypeOf[MultipartFormData[A]](Some("multipart/form-data; boundary=__X_PROCESS_STREET_BOUNDARY__"))
  }

  implicit def writeable(implicit contentType: ContentTypeOf[MultipartFormData[Array[Byte]]]): Writeable[MultipartFormData[Array[Byte]]] = {
    Writeable[MultipartFormData[Array[Byte]]]((formData: MultipartFormData[Array[Byte]]) => {

      val stringParts = formData.dataParts flatMap {
        case (key, values) => values map (new StringPart(key, _))
      }

      val fileParts = formData.files map { filePart =>
        new FilePart(filePart.key, new ByteArrayPartSource(filePart.filename, filePart.ref),filePart.contentType.get, null)
      }

      val parts = stringParts ++ fileParts

      val headers = new FluentCaseInsensitiveStringsMap().add(CONTENT_TYPE, contentType.mimeType.get)
      val entity = new MultipartRequestEntity(parts.toArray, headers)
      val outputStream = new ByteArrayOutputStream
      entity.writeRequest(outputStream)

      outputStream.toByteArray

    })(contentType)
  }

}
