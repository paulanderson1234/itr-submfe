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

import common.Constants
import models.submission.SubmissionResponse
import org.jsoup.Jsoup
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.DateFormatter

class EmailConfirmationModelSpec extends UnitSpec with DateFormatter{

  val date = desDateToDateString

  val testJson: String => JsValue = { date =>
    Json.parse(s"""
      |{
      |  "to": ["example@domain.com"],
      |  "templateId": "my-lovely-template",
      |  "parameters": {
      |    "companyName": "Test company",
      |    "date": "${date}",
      |    "formBundleRefNumber": "FORMBUNDLE123456789"
      |  },
      |  "force": false
      |}
    """.stripMargin)
  }


  "call unapply successfully to create ss Json" in {
    implicit val formats = Json.format[EmailConfirmationModel]
    val response = EmailConfirmationModel(Array("example@domain.com"), "my-lovely-template",
      EmailConfirmationModel.parameters("Test company", "FORMBUNDLE123456789"))

    val json = Json.toJson(response)
    json shouldBe testJson(date)

  }

  // form model to JSON - apply
  "call apply successfully to create model from Json" in {
    implicit val formats = Json.format[SubmissionResponse]

    val response =  testJson(date).as[EmailConfirmationModel]

    response.to(0)  shouldBe "example@domain.com"
    response.templateId  shouldBe "my-lovely-template"
    response.parameters.get(Constants.EmailConfirmationParameters.companyName).get  shouldBe "Test company"
    response.parameters.get(Constants.EmailConfirmationParameters.date).get  shouldBe date
    response.parameters.get(Constants.EmailConfirmationParameters.formBundleRefNUmber).get  shouldBe "FORMBUNDLE123456789"
  }

  "calling parameters" should {
    "return a formatted map" when {
      "it receives companyName and formbundleRef" in {
        val map = EmailConfirmationModel.parameters("Company Name G206", "087000000166")
        map.get(Constants.EmailConfirmationParameters.companyName).get shouldBe "Company Name G206"
        map.get(Constants.EmailConfirmationParameters.date).get shouldBe date
        map.get(Constants.EmailConfirmationParameters.formBundleRefNUmber).get shouldBe "087000000166"
      }
    }
  }
}
