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

package Forms
import models.submission.SubmissionResponse
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SubmissionResponseSpec extends UnitSpec {

  val testJson = """{"processingDate":"2014-12-17T09:30:47Z","formBundleNumber":"FBUND98763284"}"""

  // form json to model - unapply
  "call unapply successfully to create ss Json" in {
    implicit val formats = Json.format[SubmissionResponse]
    val response = SubmissionResponse("2014-12-17T09:30:47Z", "FBUND98763284")

    val json = Json.toJson(response)
    json.toString() shouldBe testJson

  }

  // form model to JSON - apply
  "call apply successfully to create model from Json" in {
    implicit val formats = Json.format[SubmissionResponse]

    val response =  Json.parse(testJson.toString).as[SubmissionResponse]

    response.processingDate  shouldBe "2014-12-17T09:30:47Z"
    response.formBundleNumber  shouldBe "FBUND98763284"
  }

}
