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

package views.seis

import models.seis.SEISCheckAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.CheckAnswersSpec
import utils.CountriesHelper
import play.api.i18n.Messages.Implicits._
import views.html.seis.checkAndSubmit.CheckAnswers

class CheckAnswersContactDetailsSpec extends CheckAnswersSpec {

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 4: Contact Details" +
      " when the contact details model is fully populated" in {
      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), None, None, None,
        Some(contactDetailsModel), Some(contactAddressModel), false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFour")

      // contactDetails
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-question").text() shouldBe
        Messages("page.summaryQuestion.contactDetails")

      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line0").text() shouldBe
        contactDetailsModel.fullName
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line1").text() shouldBe
        contactDetailsModel.telephoneNumber.get
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line2").text() shouldBe
        contactDetailsModel.mobileNumber.get
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line3").text() shouldBe
        contactDetailsModel.email

      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-link")
        .attr("href") shouldBe controllers.seis.routes.ContactDetailsController.show().toString

      //address
      contactDetailsTable.select("tr").get(1).getElementById("address-question").text() shouldBe
        Messages("page.summaryQuestion.contactAddress")
      contactDetailsTable.select("tr").get(1).getElementById("address-Line0").text() shouldBe
        contactAddressModel.addressline1
      contactDetailsTable.select("tr").get(1).getElementById("address-Line1").text() shouldBe
        contactAddressModel.addressline2
      contactDetailsTable.select("tr").get(1).getElementById("address-Line2").text() shouldBe
        CountriesHelper.getSelectedCountry(contactAddressModel.countryCode)
      contactDetailsTable.select("tr").get(1).getElementById("address-link")
        .attr("href") shouldBe controllers.seis.routes.ContactAddressController.show().toString
    }

    "Verify that the Check Answers page contains an empty table for Section 4: Contact Details" +
      " when the contact details model is not populated" in {
      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFour")

      contactDetailsTable.select("tr").size() shouldBe 0
    }
  }
}

