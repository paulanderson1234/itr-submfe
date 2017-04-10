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

import auth.TAVCUser
import models.submission.{Scheme, AASubmissionDetailsModel, SubmissionDetailsModel}

import org.scalatestplus.play.OneServerPerSuite
import auth.TAVCUser
import auth.ggUser.allowedAuthContext
import connectors.SubmissionConnector
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.{Json, JsString}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

class SubmissionServiceSpec extends UnitSpec with MockitoSugar with OneServerPerSuite{

  object TestSubmiisionService extends SubmissionService{
    override val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  }

  val tavcRef = "XATAVC000123456"
  implicit val hc = HeaderCarrier()
  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"
  implicit val user = TAVCUser(allowedAuthContext, internalId)

  val aASubmissionDetailsModelOne = AASubmissionDetailsModel("000000123456", "Compliance Statement",
      "2015-09-22", List(Scheme("EIS"),Scheme("UCT")), "Received", "003333333333")
  val aASubmissionDetailsModelTwo = AASubmissionDetailsModel("000000000000", "Advance Assurance",
    "2015-09-22", List(Scheme("EIS"),Scheme("UCT")), "Rejected", "003333333334")

  val combinedSubmissionModel = SubmissionDetailsModel(List(aASubmissionDetailsModelOne,aASubmissionDetailsModelTwo))

  val emptySubmissionModel = SubmissionDetailsModel(List())

  val invalidJson = Json.parse(
    """
      |{}
    """.stripMargin)

  val combinedJson = Json.parse(
    """
      |{
      |   "processingDate":"2015-09-22T10:30:06Z",
      |   "countReturned":"2",
      |   "countTotal":"2",
      |   "submissions":[
      |      {
      |         "formBundleNumber":"000000123456",
      |         "submissionType":"Compliance Statement",
      |         "submissionDate":"2015-09-22",
      |         "schemeType":[
      |            {
      |               "scheme":"EIS"
      |            },
      |            {
      |               "scheme":"UCT"
      |            }
      |         ],
      |         "status":"Received",
      |         "contactNoteReference":"003333333333"
      |      },
      |      {
      |         "formBundleNumber":"000000000000",
      |         "submissionType":"Advance Assurance",
      |         "submissionDate":"2015-09-22",
      |         "schemeType":[
      |            {
      |               "scheme":"EIS"
      |            },
      |            {
      |               "scheme":"UCT"
      |            }
      |         ],
      |         "status":"Rejected",
      |         "contactNoteReference":"003333333334"
      |      }
      |   ]
      |}
    """.stripMargin)

  val emptySubmissionsJson = Json.parse(
    """
      |{
      |"processingDate":"2015-09-22T10:30:06Z",
      |"countReturned":"2",
      |"countTotal":"2",
      |"submissions":[]
      |}
    """.stripMargin)


  "Submission service" should {
    "use the correct submission connector" in {
      SubmissionService.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Calling getEtmpSubmissionDetails" should {
    "convert the combined submission details data from json to the corresponding model" in {
      when(TestSubmiisionService.submissionConnector.getAASubmissionDetails(Matchers.any())(Matchers.any())).
        thenReturn(HttpResponse(OK, Some(combinedJson)))
      lazy val result = TestSubmiisionService.getEtmpSubmissionDetails(tavcRef)
      await(result) shouldBe Some(combinedSubmissionModel)
    }
  }

  "Calling getEtmpSubmissionDetails" should {
    "convert the empty submission details data from json to the corresponding model" in {
      when(TestSubmiisionService.submissionConnector.getAASubmissionDetails(Matchers.any())(Matchers.any())).
        thenReturn(HttpResponse(OK, Some(emptySubmissionsJson)))
      lazy val result = TestSubmiisionService.getEtmpSubmissionDetails(tavcRef)
      await(result) shouldBe Some(emptySubmissionModel)
    }
  }

  "Calling getEtmpSubmissionDetails" should {
    "return None if the Model does not validate the Json" in {
      when(TestSubmiisionService.submissionConnector.getAASubmissionDetails(Matchers.any())(Matchers.any())).
        thenReturn(HttpResponse(OK, Some(invalidJson)))
      lazy val result = TestSubmiisionService.getEtmpSubmissionDetails(tavcRef)
      await(result) shouldBe None
    }
  }

}
