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

import controllers.routes
import models.submission.SubmissionResponse
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.checkAndSubmit.Acknowledgement

class AcknowledgementSpec extends ViewSpec {

  val submissionResponse = SubmissionResponse("2014-12-17T09:30:47Z","FBUND09889765")

  "The Acknowledgement page" should {

    "contain the correct elements when loaded" in {

      lazy val page = Acknowledgement(submissionResponse)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))
      //title
      document.title() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
      //banner
      document.body.getElementById("submission-confirmation").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.submissionConfirmation")
      document.body.getElementById("ref-number-heading").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.refNumberHeading")
      document.body.getElementById("ref-number").text() shouldBe submissionResponse.formBundleNumber
      //legal text
      document.body.getElementById("legal-not-complete").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.application.not.complete")
      //'what to do next' section
      document.body.getElementById("what-next").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.toDoNext")
      document.body.getElementById("prior-to-review").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.priorToReview")
      //supporting docs
      document.body.getElementById("remember-to-include").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.remember")
      document.body.getElementById("business-plan").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      document.body.getElementById("company-accounts").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      document.body.getElementById("shareholder-agree").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      document.body.getElementById("memorandum-articles").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      document.body.getElementById("prospectus-docs").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.five")
      //email to
      document.body.getElementById("email-to").text() shouldBe getExternalEmailText(Messages("page.checkAndSubmit.acknowledgement.emailTo"),
        Messages("page.checkAndSubmit.acknowledgement.subjectLineInclude"))
      document.body.getElementById("email-to").getElementById("email-to-ref").attr("href") shouldEqual "mailto:enterprise.centre@hmrc.gsi.gov.uk"


      //hmrc address
      document.body.getElementById("alternative").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.alternative")
      document.body.getElementById("hmrc-address").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.hmrc") ++
        " " ++ Messages("page.checkAndSubmit.acknowledgement.hmrc.address1") ++ " " ++ Messages("page.checkAndSubmit.acknowledgement.hmrc.address2") ++
        " " ++ Messages("page.checkAndSubmit.acknowledgement.hmrc.address3") ++ " " ++ Messages("page.checkAndSubmit.acknowledgement.hmrc.postcode")

      //waiting times
      document.body.getElementById("waiting-time").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.waitingTime")
      document.body.getElementById("course-of-action").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.courseOfAction")

      //get help
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
    }
  }

}
