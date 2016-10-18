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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package views

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.helpers.CheckAnswersHelper
import play.api.test.Helpers._

class CheckAnswersContactDetailsSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with BeforeAndAfterEach with CheckAnswersHelper {

  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 4: Contact Details" +
      " when the contact details model is fully populated" in new Setup {
      val document: Document = {
        previousSchemeSetup()
        investmentSetup()
        contactDetailsSetup(Some(contactDetailsModel))
        companyDetailsSetup()
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFour")

      // fullname
      contactDetailsTable.select("tr").get(0).getElementById("name-question").text() shouldBe
        Messages("page.summaryQuestion.name")
      contactDetailsTable.select("tr").get(0).getElementById("name-answer").text() shouldBe
        contactDetailsModel.fullName
      contactDetailsTable.select("tr").get(0).getElementById("name-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString

      // telephone
      contactDetailsTable.select("tr").get(1).getElementById("telephone-question").text() shouldBe
        Messages("page.summaryQuestion.telephone")
      contactDetailsTable.select("tr").get(1).getElementById("telephone-answer").text() shouldBe
        contactDetailsModel.telephoneNumber
      contactDetailsTable.select("tr").get(1).getElementById("telephone-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString

      // email
      contactDetailsTable.select("tr").get(2).getElementById("email-question").text() shouldBe
        Messages("page.summaryQuestion.email")
      contactDetailsTable.select("tr").get(2).getElementById("email-answer").text() shouldBe
        contactDetailsModel.email
      contactDetailsTable.select("tr").get(2).getElementById("email-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString

      //address
      contactDetailsTable.select("tr").get(3).getElementById("address-question").text() shouldBe
        Messages("page.summaryQuestion.contactAddress")
      contactDetailsTable.select("tr").get(3).getElementById("address-Line0").text() shouldBe
        "Company Name Ltd."
      contactDetailsTable.select("tr").get(3).getElementById("address-Line1").text() shouldBe
        "2 Telford Plaza"
      contactDetailsTable.select("tr").get(3).getElementById("address-Line2").text() shouldBe
        "Lawn Central"
      contactDetailsTable.select("tr").get(3).getElementById("address-Line3").text() shouldBe
        "Telford"
      contactDetailsTable.select("tr").get(3).getElementById("address-Line4").text() shouldBe
        "TF3 4NT"
      contactDetailsTable.select("tr").get(3).getElementById("address-link")
        .attr("href") shouldBe routes.ConfirmCorrespondAddressController.show().toString
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 4: Contact Details" +
      " when the contact details model is not populated" in new Setup {
      val document: Document = {
        previousSchemeSetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFour")

      // fullname
      contactDetailsTable.select("tr").get(0).getElementById("name-question").text() shouldBe
        Messages("page.summaryQuestion.name")
        contactDetailsTable.select("tr").get(0).getElementById("name-answer").text() shouldBe
        notAvailableMessage
      contactDetailsTable.select("tr").get(0).getElementById("name-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString

      // telephone
      contactDetailsTable.select("tr").get(1).getElementById("telephone-question").text() shouldBe
        Messages("page.summaryQuestion.telephone")
      contactDetailsTable.select("tr").get(1).getElementById("telephone-answer").text() shouldBe
        notAvailableMessage
      contactDetailsTable.select("tr").get(1).getElementById("telephone-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString

      // email
      contactDetailsTable.select("tr").get(2).getElementById("email-question").text() shouldBe
        Messages("page.summaryQuestion.email")
      contactDetailsTable.select("tr").get(2).getElementById("email-answer").text() shouldBe
        notAvailableMessage
      contactDetailsTable.select("tr").get(2).getElementById("email-link")
        .attr("href") shouldBe routes.ContactDetailsController.show().toString


      // address
      contactDetailsTable.select("tr").get(3).getElementById("address-question").text() shouldBe
        Messages("page.summaryQuestion.contactAddress")
      contactDetailsTable.select("tr").get(3).getElementById("address-answer").text() shouldBe
        notAvailableMessage
      contactDetailsTable.select("tr").get(3).getElementById("address-link")
        .attr("href") shouldBe routes.ConfirmCorrespondAddressController.show().toString
    }
  }

}