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

import models.OperatingCostsModel

object KnowledgeIntensiveCalculator extends KnowledgeIntensiveCalculator{
  val percentageFifteen = 0.15
  val percentageTen = 0.10
}

trait KnowledgeIntensiveCalculator {

  val percentageFifteen: Double
  val percentageTen: Double

  def checkRAndDCosts(operatingCosts1stYear: Int,operatingCosts2ndYear: Int,operatingCosts3rdYear: Int,
                      rAndDCosts1stYear:Int, rAndDCosts2ndYear:Int, rAndDCosts3rdYear:Int): Boolean = {

    val costs = List((operatingCosts1stYear, rAndDCosts1stYear),
      (operatingCosts2ndYear, rAndDCosts2ndYear),
      (operatingCosts3rdYear, rAndDCosts3rdYear))

    /** Checks that all operating costs are greater than zero. 'forall' short hand for map and contains **/
    def validOperatingCosts: Boolean = costs.forall {case (operatingCosts,rAndDCosts) => operatingCosts > 0}

    /** Checks if one element is true. 'exists' short hand for map and contains **/
    def operatingCostConditionFifteen: Boolean = {
      costs.exists {case (operatingCosts,rAndDCosts) => operatingCosts.toDouble * percentageFifteen <= rAndDCosts.toDouble}
    }

    /** Checks that all elements are true. 'forall' short hand for map and contains **/
    def operatingCostConditionTen: Boolean = {
      costs.forall {case (operatingCosts,rAndDCosts) => operatingCosts.toDouble * percentageTen <= rAndDCosts.toDouble}
    }

    if (validOperatingCosts) operatingCostConditionFifteen||operatingCostConditionTen else false
  }
}
