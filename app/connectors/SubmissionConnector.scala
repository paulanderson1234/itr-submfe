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

package connectors
import config.{FrontendAppConfig, WSHttp}
import models.{AnnualTurnoverCostsModel, ProposedInvestmentModel}
import models.submission.{DesSubmitAdvancedAssuranceModel, Submission}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.utils.UriEncoding
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

object SubmissionConnector extends SubmissionConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.submissionUrl
  override lazy val http = WSHttp
}

trait SubmissionConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut

  def urlEncode(s :String) :String = UriEncoding.encodePathSegment(s,"UTF-8")

  def validateKiCostConditions(operatingCostYear1: Int, operatingCostYear2: Int, operatingCostYear3: Int,
                               rAndDCostsYear1: Int, rAndDCostsYear2: Int, rAndDCostsYear3: Int)
                              (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-ki-costs/" +
      s"operating-costs/${urlEncode(operatingCostYear1.toString)}/${urlEncode(operatingCostYear2.toString)}/${urlEncode(operatingCostYear3.toString)}/" +
      s"rd-costs/${urlEncode(rAndDCostsYear1.toString)}/${urlEncode(rAndDCostsYear2.toString)}/${urlEncode(rAndDCostsYear3.toString)}")
  }

  def validateSecondaryKiConditions(hasPercentageWithMasters: Boolean,
                                    hasTenYearPlan: Boolean)
                                   (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-secondary-conditions/has-percentage-with-masters/" +
      s"${urlEncode(hasPercentageWithMasters.toString)}/has-ten-year-plan/${urlEncode(hasTenYearPlan.toString)}")
  }

  def checkLifetimeAllowanceExceeded(hadPrevRFI: Boolean, isKi: Boolean, previousInvestmentSchemesTotal: Int,
                                     proposedAmount: Int)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/lifetime-allowance/lifetime-allowance-checker/had-previous-rfi/" +
      s"$hadPrevRFI/is-knowledge-intensive/$isKi/previous-schemes-total/${urlEncode(previousInvestmentSchemesTotal.toString)}/proposed-amount/${urlEncode(proposedAmount.toString)}")

  }

  def checkAveragedAnnualTurnover(proposedInvestmentAmount: ProposedInvestmentModel, annualTurnoverCostsModel: AnnualTurnoverCostsModel)
                                 (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/averaged-annual-turnover/check-averaged-annual-turnover/" +
      s"proposed-investment-amount/${urlEncode(proposedInvestmentAmount.investmentAmount.toString)}/annual-turn-over/${urlEncode(annualTurnoverCostsModel.amount1)}" +
      s"/${urlEncode(annualTurnoverCostsModel.amount2)}/${urlEncode(annualTurnoverCostsModel.amount3)}/${urlEncode(annualTurnoverCostsModel.amount4)}/${urlEncode(annualTurnoverCostsModel.amount5)}")
  }

  //TODO: put all these methods in a service?
  def submitAdvancedAssurance(submissionRequest: Submission, tavcReferenceNumber: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    if(tavcReferenceNumber.isEmpty) {
      Logger.warn("[SubmissionConnector][submitAdvancedAssurance] An empty tavcReferenceNumber was passed")
    }
    require(tavcReferenceNumber.nonEmpty, "[SubmissionConnector][submitAdvancedAssurance] An empty tavcReferenceNumber was passed")

    val json = Json.toJson(submissionRequest)
    val targetSubmissionModel = Json.parse(json.toString()).as[DesSubmitAdvancedAssuranceModel]
    http.POST[JsValue, HttpResponse](s"$serviceUrl/investment-tax-relief/advanced-assurance/${urlEncode(tavcReferenceNumber)}/submit", Json.toJson(targetSubmissionModel))
  }

  def getRegistrationDetails(safeID: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief/registration/registration-details/safeid/${urlEncode(safeID)}")
  }

  def checkMarketCriteria(newGeographical: Boolean, newProduct: Boolean)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/market-criteria/new-geographical/${urlEncode(newGeographical.toString)}/new-product/${urlEncode(newProduct.toString)}")
  }

}
