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
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SubmissionRequestModelSpec extends UnitSpec {

  val testJson = """{"contactDetails":{"forename":"first","surname":"last","telephoneNumber":"07000 111222","email":"test@test.com"},"yourCompanyNeedModel":{"needAAorCS":"AA"}}"""

  // form json to model - unapply
  "call unapply successfully to create as Json" in {
    implicit val formats = Json.format[SubmissionRequest]
    val cd = ContactDetailsModel("first", "last", Some("07000 111222"), None , "test@test.com")
    val yd = YourCompanyNeedModel("AA")
    val sub = new SubmissionRequest(cd, yd)

    val json = Json.toJson(sub)
    json.toString() shouldBe testJson
  }


  // form model to JSON - apply
  "call apply successfully to create model from Json" in {
    implicit val formats = Json.format[SubmissionResponse]

    val request =  Json.parse(testJson.toString).as[SubmissionRequest]

    request.contactDetails.email  shouldBe "test@test.com"
    request.contactDetails.telephoneNumber.get  shouldBe "07000 111222"
    request.contactDetails.forename  shouldBe "first"
    request.contactDetails.surname  shouldBe "last"
    request.yourCompanyNeedModel.needAAorCS  shouldBe "AA"
  }
}
