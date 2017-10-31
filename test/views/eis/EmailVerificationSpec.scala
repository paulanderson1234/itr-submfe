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

import models.EmailVerificationModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers.{contentAsString, _}
import views.helpers.ViewSpec
import views.html.eis.verification.EmailVerification

class EmailVerificationSpec extends ViewSpec {

  val email = "test@test.com"
  val emailRedirectOption = 123

  lazy val page = EmailVerification(EmailVerificationModel(email), emailRedirectOption)(fakeRequest,applicationMessages)
  lazy val document = Jsoup.parse(contentAsString(page))

  "Email verification page" should {
    "show when contact details passed" in {
      document.title() shouldBe Messages("page.verification.EmailVerification.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.verification.EmailVerification.heading")
      document.body.getElementById("email-one").text shouldBe Messages("page.verification.EmailVerification.info.one") +
        s" $email" + Messages("page.verification.EmailVerification.info.two")
      document.body.getElementById("help").text shouldBe Messages("page.verification.EmailVerification.help.link")

      document.body.getElementById("help-text-one").text shouldBe Messages("page.verification.EmailVerification.help.text.one") +
        " " + Messages("page.verification.EmailVerification.help.text.two") +
        " " + Messages("page.verification.EmailVerification.help.text.three") +
        " " + Messages("page.verification.EmailVerification.help.text.four")

      document.body.getElementById("email-help-link-one").text shouldBe Messages("page.verification.EmailVerification.help.text.two")
      document.body.getElementById("email-help-link-two").text shouldBe Messages("page.verification.EmailVerification.help.text.four")

      document.body.getElementById("email-help-link-one").attr("href") shouldEqual
        controllers.eis.routes.ContactDetailsController.show().url

      document.body.getElementById("email-help-link-two").attr("href") shouldEqual
        controllers.eis.routes.EmailVerificationController.verify(emailRedirectOption).url
    }
  }
}
