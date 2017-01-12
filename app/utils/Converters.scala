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

import models.{AnnualTurnoverCostsModel, OperatingCostsModel}
import models.submission.{AnnualCostModel, CostModel, TurnoverCostModel}

object Converters extends Converters{

}

trait Converters {

  def operatingCostsToList(operatingCostsModel: OperatingCostsModel): List[AnnualCostModel] = {

    require(operatingCostsModel.firstYear.toInt >= 1000 && operatingCostsModel.firstYear.toInt <= 9999, "most recent year must be a 4 digit integer")

    List(
      AnnualCostModel(operatingCostsModel.firstYear, CostModel(amount = operatingCostsModel.operatingCosts1stYear),
        CostModel(amount = operatingCostsModel.rAndDCosts1stYear)),
      AnnualCostModel(operatingCostsModel.secondYear, CostModel(amount = operatingCostsModel.operatingCosts2ndYear),
        CostModel(amount = operatingCostsModel.rAndDCosts2ndYear)),
      AnnualCostModel(operatingCostsModel.thirdYear, CostModel(amount = operatingCostsModel.operatingCosts3rdYear),
        CostModel(amount = operatingCostsModel.rAndDCosts3rdYear)))
  }

  def turnoverCostsToList(turnoverCostModel: AnnualTurnoverCostsModel): List[TurnoverCostModel] = {

    require(turnoverCostModel.firstYear.toInt >= 1000 && turnoverCostModel.firstYear.toInt <= 9999, "most recent year must be a 4 digit integer")


    List(
      TurnoverCostModel(turnoverCostModel.firstYear, CostModel(amount = turnoverCostModel.amount1)),
      TurnoverCostModel(turnoverCostModel.secondYear, CostModel(amount = turnoverCostModel.amount2)),
      TurnoverCostModel(turnoverCostModel.thirdYear, CostModel(amount = turnoverCostModel.amount3)),
      TurnoverCostModel(turnoverCostModel.fourthYear, CostModel(amount = turnoverCostModel.amount4)),
      TurnoverCostModel(turnoverCostModel.fifthYear, CostModel(amount = turnoverCostModel.amount5))
     )
  }
}
