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

package views

import auth.{Enrolment, Identifier, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import controllers.{AcknowledgementController, routes}
import controllers.helpers.{FakeRequestHelper, TestHelper}
import models.submission.SubmissionResponse
import models.{ContactDetailsModel, SubmissionRequest, YourCompanyNeedModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._

import scala.concurrent.Future

  class AcknowledgementSpec extends UnitSpec with WithFakeApplication with Mockito with FakeRequestHelper{

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockSubmission = mock[SubmissionConnector]

  val contactValid = ContactDetailsModel("Frank","The Tank","01384 555678","email@nothingness.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequest = SubmissionRequest(contactValid,yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17T09:30:47Z","FBUND09889765")

  class SetupPage {

    val controller = new AcknowledgementController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
      val submissionConnector: SubmissionConnector = mockSubmission
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

//    "The Acknowledgement page" should {
//
//      "contain the correct elements when loaded" in new SetupPage {
//        val document: Document = {
//          //val userId = s"user-${UUID.randomUUID}"
//          when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
//            .thenReturn(Future.successful(Option(contactValid)))
//          when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
//            .thenReturn(Future.successful(Option(yourCompanyNeed)))
//          when(mockSubmission.submitAdvancedAssurance(Matchers.eq(submissionRequest))(Matchers.any()))
//            .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
//          val result = controller.show.apply(authorisedFakeRequest)
//          Jsoup.parse(contentAsString(result))
//        }
//
//        //title
//        document.title() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
//        //banner
//        document.body.getElementById("submission-confirmation").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.submissionConfirmation")
//        document.body.getElementById("ref-number-heading").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.refNumberHeading")
//        document.body.getElementById("ref-number").text() shouldBe "FBUND09889765"
//        document.body.getElementById("confirm-email").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.confirmEmail")
//        //'what to do next' section
//        document.body.getElementById("what-next").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.toDoNext")
//        document.body.getElementById("email-to").text() shouldBe TestHelper.getExternalEmailText(Messages("page.checkAndSubmit.acknowledgement.emailTo"))
//        document.body.getElementById("email-to").getElementById("email-to-ref").attr("href") shouldEqual "mailto:enterprise.centre@hmrc.gsi.gov.uk"
//        document.body.getElementById("subject-line-include").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.subjectLineInclude")
//        document.body.getElementById("application-reference-number").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.applicationReferenceNumber")
//        document.body.getElementById("company-name").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.companyName")
//        //dropdown
//        document.body.getElementById("help").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.help")
//        document.body.getElementById("send-us").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.sendUs")
//        document.body.getElementById("business-plan").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.businessPlan")
//        document.body.getElementById("company-accounts").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.companyAccounts")
//        document.body.getElementById("subsidiary-accounts").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.subsidiaryAccounts")
//        document.body.getElementById("shareholder-agreements").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.shareholderAgreements")
//        document.body.getElementById("articles-of-association").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.articlesOfAssociation")
//        document.body.getElementById("docs-prospectus").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.docsProspectus")
//        //waiting times
//        document.body.getElementById("waiting-time").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.waitingTime")
//        document.body.getElementById("course-of-action").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.courseOfAction")
//        //back link
//        document.body.getElementById("back-link").attr("href") shouldEqual routes.CheckAnswersController.show.toString()
//        //get help
//        document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
//    }
//  }


}
