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

package utils

import models.OperatingCostsModel
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class CostFormatterSpec extends UnitSpec with MockitoSugar with Matchers{

  // Encapulate test fixtures in private variables.
  private val  presentCost = ("2560","2015-2016")
  private val pastCost = ("100","2000-2001")
  private val invalidCost = ("Not a number","Blurb")

  private val presentCostFormatted = "£2,560 in 2015-2016 tax year"
  private val pastCostFormatted = "£100 in 2000-2001 tax year"

  "The Cost Formatter" should {

    "Return the operating and R&D costs as a formatted string in the form '£xxx in yyy tax year'" in {
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(presentCost._1,presentCost._2) shouldBe presentCostFormatted
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(pastCost._1,pastCost._2) shouldBe pastCostFormatted
      intercept[NumberFormatException] {
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(invalidCost._1, invalidCost._2)
      }
    }
  }

}
