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

package common

import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.test.UnitSpec

class KnowledgeIntensiveCalculatorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val rAndDCostsZero = List(100, 100, 100, 0, 0, 0)
  val rAndDCostsTen = List(100, 100, 100, 10, 10, 10)
  val rAndDCostsFifteen = List(100, 100, 100, 15, 0, 0)
  val negativeOperatingCosts = List(-100, -100, -100, 0, 0, 0)

  def operatingCostsCheck(costs: List[Int])(test: Boolean => Any) {
    val result = KnowledgeIntensiveCalculator.checkRAndDCosts(costs(0),costs(1),costs(2),costs(3),costs(4),costs(5))
    test(result)
  }

  "Sending a 0 percent OperatingCosts" should {
    "return false" in {

      operatingCostsCheck(rAndDCostsZero)(
        result => {
          result shouldBe false
        }
      )
    }
  }

  "Sending a 10 percent OperatingCosts" should {
    "return true" in {

      operatingCostsCheck(rAndDCostsTen)(
        result => {
          result shouldBe true
        }
      )
    }
  }

  "Sending a 15 percent OperatingCosts" should {
    "return true" in {

      operatingCostsCheck(rAndDCostsFifteen)(
        result => {
          result shouldBe true
        }
      )
    }
  }

  "Sending negative OperatingCosts" should {
    "return false" in {

      operatingCostsCheck(negativeOperatingCosts)(
        result => {
          result shouldBe false
        }
      )
    }
  }

}
