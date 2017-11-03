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

package models

import common.Constants
import models.internal.CSApplicationModel
import uk.gov.hmrc.play.test.UnitSpec

class CSApplicationModelSpec extends UnitSpec{

  "Instantiating a CSApplicationModel" when {

    "false and no scheme type is passed" should {
      "return a valid CSApplicationModel" in {
        noException should be thrownBy CSApplicationModel(false, None)
      }
    }

    "false and a scheme type is passed" should {
      "throw an Exception" in {
        intercept[IllegalArgumentException] {
          CSApplicationModel(false, Some(Constants.schemeTypeEis))
        }
      }
    }

    "true and a scheme type is passed" should {
      "return a valid CSApplicationModel" in {
        noException should be thrownBy CSApplicationModel(true, Some(Constants.schemeTypeEis))
      }
    }

    "true and no scheme type is passed" should {
      "throw an Exception" in {
        intercept[IllegalArgumentException] {
          CSApplicationModel(true, None)
        }
      }
    }
  }

}