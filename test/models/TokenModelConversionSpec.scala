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

package models
import models.submission.SubmissionResponse
import models.throttling.TokenModel
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class TokenModelConversionSpec extends UnitSpec {

  val testJson = """{"_id":"TOK123456789"}"""
  val id = "TOK123456789"

  // form json to model - unapply
  "call unapply successfully to create as Json" in {
    implicit val formats = Json.format[SubmissionRequest]
    val tokenModel = TokenModel(id)
    val json = Json.toJson(tokenModel)
    json.toString() shouldBe testJson
  }


  // form model to JSON - apply
  "call apply successfully to create model from Json" in {
    implicit val formats = Json.format[TokenModel]
    val request =  Json.parse(testJson.toString).as[TokenModel]
    request._id shouldBe id
  }
}
