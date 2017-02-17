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
import play.api.mvc.{Action, AnyContent, Request, Result}
import config.AppConfig
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext, AuthenticationProvider, TaxRegime}
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future
import connectors.{EnrolmentConnector, S4LConnector}

import scala.concurrent.ExecutionContext.Implicits.global

trait AuthorisedAndEnrolledForTAVC extends Actions {

  val enrolmentConnector: EnrolmentConnector
  val applicationConfig: AppConfig
  val postSignInRedirectUrl: String = applicationConfig.introductionUrl
  val notEnrolledRedirectUrl: String = applicationConfig.subscriptionUrl
  val s4lConnector: S4LConnector
  val acceptedFlows: Seq[Seq[Flow]]

  private type PlayRequest = Request[AnyContent] => Result
  private type UserRequest = TAVCUser => PlayRequest
  private type AsyncPlayRequest = Request[AnyContent] => Future[Result]
  private type AsyncUserRequest = TAVCUser => AsyncPlayRequest

  private lazy val pageVisibilityPredicate = new TAVCCompositePageVisibilityPredicate(s4lConnector,acceptedFlows, authConnector)

  class AuthorisedAndEnrolled(regime: TaxRegime) {
    def async(action: AsyncUserRequest): Action[AnyContent] = {
      AuthorisedFor(regime, pageVisibilityPredicate).async {
        authContext: AuthContext => implicit request => enrolledCheck {
          case Enrolled => getInternalId(authContext).flatMap{internalId =>
            action(TAVCUser(authContext,internalId))(request)
          }
          case NotEnrolled => Future.successful(Redirect(notEnrolledRedirectUrl))
        }
      }
    }

    def apply(action: UserRequest): Action[AnyContent] = async(user => request => Future.successful(action(user)(request)))
  }

  object AuthorisedAndEnrolled extends AuthorisedAndEnrolled(TAVCRegime)

  def getInternalId(authContext: AuthContext)(implicit hc: HeaderCarrier): Future[String] =
  {
    for {
      userIds <- authConnector.getIds[UserIDs](authContext)
    } yield userIds.internalId

  }

  def enrolledCheck(f: EnrolmentResult => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    for {
      authority <- authConnector.currentAuthority
      enrolment <- enrolmentConnector.getTAVCEnrolment(authority.fold("")(_.uri))
      result <- f(mapToEnrolledResult(enrolment))
    } yield result
  }

  def getTavCReferenceNumber()(implicit hc: HeaderCarrier): Future[String] = {
    for {
      authority <- authConnector.currentAuthority
      tavcRef <- enrolmentConnector.getTavcReferenceNumber(authority.fold("")(_.uri))
    } yield tavcRef
  }

  private def mapToEnrolledResult: Option[Enrolment] => EnrolmentResult = {
    case Some(tavcEnrolment) if tavcEnrolment.state == "Activated" => Enrolled
    case _ => NotEnrolled
  }

  implicit private def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

  val tavcAuthProvider: GovernmentGatewayProvider = new GovernmentGatewayProvider(postSignInRedirectUrl, applicationConfig.ggSignInUrl)

  trait TAVCRegime extends TaxRegime {
    override def isAuthorised(accounts: Accounts): Boolean = true
    override def authenticationType: AuthenticationProvider = tavcAuthProvider
  }

  object TAVCRegime extends TAVCRegime
}
