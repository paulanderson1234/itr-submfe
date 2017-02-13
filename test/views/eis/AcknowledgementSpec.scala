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

package views.eis

import models.submission.SubmissionResponse
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eis.checkAndSubmit.Acknowledgement

class AcknowledgementSpec extends ViewSpec {

  val submissionResponse = SubmissionResponse("2014-12-17T09:30:47Z","FBUND09889765")

  "The Acknowledgement page" should {

    "contain the correct elements when loaded" in {

      lazy val page = Acknowledgement(submissionResponse)(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))
      //title
      document.title() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
      //banner
      document.body.getElementById("submission-confirmation").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.submissionConfirmation")
      document.body.getElementById("ref-number-heading").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.refNumberHeading")
      document.body.getElementById("ref-number").text() shouldBe submissionResponse.formBundleNumber
      //legal text
      document.body.getElementById("legal-not-complete").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.application.not.complete")

      //supporting docs
      document.body.getElementById("supporting-docs").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.supporting.docs.heading")
      document.body.getElementById("remember-to-include").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.review")
      document.body.getElementById("business-plan").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.doc.one")
      document.body.getElementById("company-accounts").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.doc.two")
      document.body.getElementById("shareholder-agree").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.doc.three")
      document.body.getElementById("memorandum-articles").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.doc.four")
      document.body.getElementById("prospectus-docs").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.doc.five")

      //email to
      document.body.getElementById("email-docs").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.email.docs.heading")
      document.body.getElementById("email-to").getElementById("email-docs-to").text() shouldEqual (Messages("page.checkAndSubmit.acknowledgement.emailTo"))
      document.body.getElementById("email-to").getElementById("email-to-ref").attr("href") shouldEqual "mailto:enterprise.centre@hmrc.gsi.gov.uk"
      document.body.getElementById("email-docs-include").text() shouldBe (Messages("page.checkAndSubmit.acknowledgement.email.include"))
      document.body.getElementById("email-name").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.email.one")
      document.body.getElementById("email-utr").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.email.two")
      document.body.getElementById("email-whitelister").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.email.three")
      document.body.getElementById("email-docs-size").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.email.size")

      //post to
      document.body.getElementById("post-docs").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.post.docs.heading")
      document.body.getElementById("post-to").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.post.to")

      //waiting times
      document.body.getElementById("waiting-time").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.waitingTime.heading")
      document.body.getElementById("course-of-action").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.courseOfAction")

      //get help
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

      //finish button
      document.body.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.button.confirm")
    }
  }

}
