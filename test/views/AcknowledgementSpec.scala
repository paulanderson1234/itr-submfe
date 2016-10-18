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
import controllers.helpers.FakeRequestHelper
import fixtures.SubmissionFixture
import models.submission.SubmissionResponse
import org.jsoup.Jsoup
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import views.helpers.ViewTestHelper
import views.html.checkAndSubmit.Acknowledgement

class AcknowledgementSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with SubmissionFixture with ViewTestHelper {

  val submissionResponse = SubmissionResponse("2014-12-17T09:30:47Z","FBUND09889765")

    "The Acknowledgement page" should {

      "contain the correct elements when loaded" in{

        lazy val page = Acknowledgement(submissionResponse)(fakeRequest)
        lazy val document = Jsoup.parse(contentAsString(page))
        //title
        document.title() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
        //banner
        document.body.getElementById("submission-confirmation").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.submissionConfirmation")
        document.body.getElementById("ref-number-heading").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.refNumberHeading")
        document.body.getElementById("ref-number").text() shouldBe submissionResponse.formBundleNumber

        //'what to do next' section
        document.body.getElementById("what-next").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.toDoNext")
        document.body.getElementById("email-to").text() shouldBe getExternalEmailText(Messages("page.checkAndSubmit.acknowledgement.emailTo"))
        document.body.getElementById("email-to").getElementById("email-to-ref").attr("href") shouldEqual "mailto:enterprise.centre@hmrc.gsi.gov.uk"
        document.body.getElementById("subject-line-include").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.subjectLineInclude")
        document.body.getElementById("application-reference-number").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.applicationReferenceNumber")
        document.body.getElementById("company-name").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.companyName")
        //dropdown
        document.body.getElementById("help").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.help")
        document.body.getElementById("send-us").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.sendUs")
        document.body.getElementById("business-plan").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.businessPlan")
        document.body.getElementById("company-accounts").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.companyAccounts")
        document.body.getElementById("subsidiary-accounts").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.subsidiaryAccounts")
        document.body.getElementById("shareholder-agreements").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.shareholderAgreements")
        document.body.getElementById("articles-of-association").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.articlesOfAssociation")
        document.body.getElementById("docs-prospectus").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.docsProspectus")
        //waiting times
        document.body.getElementById("waiting-time").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.waitingTime")
        document.body.getElementById("course-of-action").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.courseOfAction")
        //back link
        document.body.getElementById("back-link").attr("href") shouldEqual routes.CheckAnswersController.show().url
        //get help
        document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
    }
  }

}
