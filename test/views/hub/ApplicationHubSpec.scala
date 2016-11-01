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

import java.util.UUID

import auth.MockAuthConnector
import builders.SessionBuilder
import config.{AppConfig, FrontendAppConfig}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.{ApplicationHubController, IntroductionController}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.helpers.ViewSpec
import views.html.hubPartials.{ApplicationHubExisting, ApplicationHubNew}
import views.html.introduction.ApplicationHub

class ApplicationHubSpec extends ViewSpec {


  "The Application Hub page" should {

    "Verify that hub page contains the correct elements when a 'hub new' partial is passed to it" in {
      lazy val view = ApplicationHub(ApplicationHubNew()(fakeRequest))(fakeRequest)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
    }

    "Verify that hub page contains the correct elements when a 'hub existing' partial is passed to it" in {
      lazy val view = ApplicationHub(ApplicationHubExisting()(fakeRequest))(fakeRequest)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
    }
  }
}
