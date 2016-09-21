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

package Forms
import models.SubmissionResponse
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SubmissionResponseSpec extends UnitSpec {

  val testJson = """{"status":true,"formBundleId":"FBUND98763284","message":"Submission Request Successful"}"""

  // form json to model - unapply
  "call unapply successfully to create ss Json" in {
    implicit val formats = Json.format[SubmissionResponse]
    val response = SubmissionResponse(true, "FBUND98763284", "Submission Request Successful")

    val json = Json.toJson(response)
    println(json)
    json.toString() shouldBe testJson

  }

  // form model to JSON - apply
  "call apply successfully to create model from Json" in {
    implicit val formats = Json.format[SubmissionResponse]

    val response =  Json.parse(testJson.toString()).as[SubmissionResponse]

    response.status  shouldBe true
    response.message  shouldBe "Submission Request Successful"
    response.formBundleId  shouldBe "FBUND98763284"
  }

}