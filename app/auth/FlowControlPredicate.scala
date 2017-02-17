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

package auth

import auth.authModels.UserIDs
import common.KeystoreKeys
import config.FrontendGlobal.internalServerErrorTemplate
import connectors.S4LConnector
import models.submission.SchemeTypesModel
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FlowControlPredicate(s4lConnector: S4LConnector, acceptedFlows: Seq[Seq[Flow]], authConnector: AuthConnector) extends PageVisibilityPredicate {

  override def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] = {

    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

    if (acceptedFlows.nonEmpty && acceptedFlows.contains(Seq(ALLFLOWS))) {
      Future(PageIsVisible)
    } else {
      getPageVisibility(authContext).recover {
        case e: Exception => PageBlocked(error(request))
      }
    }
  }

  private def getPageVisibility(authContext: AuthContext)(implicit hc: HeaderCarrier) : Future[PageVisibilityResult] = {
    getInternalId(authContext).flatMap {
      internalID =>
        implicit val user = TAVCUser(authContext, internalID)
        s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes).map {
          selectedSchemes =>
            if (acceptedFlows.nonEmpty) {
              if (selectedSchemes.isDefined) {
                if (flowToSchemeTypesModel(acceptedFlows).contains(selectedSchemes.get)) PageIsVisible
                else PageBlocked(redirect)
              }
              else PageBlocked(redirect)
            }
            else PageIsVisible
        }
    }
  }

  private def flowToSchemeTypesModel(acceptedFlows: Seq[Seq[Flow]], index: Int = 0, output: Seq[SchemeTypesModel] = Seq()): Seq[SchemeTypesModel] = {
    val schemeTypeModel = SchemeTypesModel(
      eis = acceptedFlows(index).contains(EIS),
      seis = acceptedFlows(index).contains(SEIS),
      vct = acceptedFlows(index).contains(VCT),
      sitr = acceptedFlows(index).contains(SITR)
    )
    if(index < acceptedFlows.length - 1) flowToSchemeTypesModel(acceptedFlows, index + 1, output:+ schemeTypeModel)
    else output:+ schemeTypeModel
  }

  private def getInternalId(authContext: AuthContext)(implicit hc: HeaderCarrier): Future[String] =
  {
    for {
      userIds <- authConnector.getIds[UserIDs](authContext)
    } yield userIds.internalId

  }

  private def redirect = Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
  private def error(request: Request[AnyContent]) = Future.successful(InternalServerError(internalServerErrorTemplate(request)))

}
