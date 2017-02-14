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

package controllers.predicates

import controllers.helpers.BaseSpec
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._

class FeatureSwitchSpec extends BaseSpec {

  object TestControllerOn extends FeatureSwitch {

    def test: Action[AnyContent] = featureSwitch(featureEnabled = true) { Action.apply { implicit request =>
        Ok
      }
    }
  }

  object TestControllerOff extends FeatureSwitch {

    def test: Action[AnyContent] = featureSwitch(featureEnabled = false) { Action.apply { implicit request =>
        Ok
      }
    }
  }

  "seisFeatureSwitch" when {

    "the SEIS feature is enabled" should {

      "return the controller's action" in {
        val result = TestControllerOn.test.apply(fakeRequest)
        status(result) shouldBe OK

      }

    }

    "the SEIS feature is disabled" should {

      "redirect to the hub page" in {
        val result = TestControllerOff.test.apply(fakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
      }

    }

  }

}
