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

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, TAVCUser, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector, FrontendGlobal}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import models.{ContactDetailsModel, EmailVerificationModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.EmailVerificationService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.verification.EmailVerification

import scala.concurrent.Future


object EmailVerificationController extends EmailVerificationController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val emailVerificationService = EmailVerificationService
}

trait EmailVerificationController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val emailVerificationService: EmailVerificationService

  def verify(urlPosition: Int) : Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    urlPosition match {
        case Constants.ContactDetailsReturnUrl => {
          val result = verifyEmailStatus(applicationConfig.emailVerificationEisReturnUrlOne)
          processSendEmailVerification(result)
        }
        case Constants.CheckAnswersReturnUrl => {
          val result = verifyEmailStatus(applicationConfig.emailVerificationEisReturnUrlTwo)
          processSubmitEmailVerification(result)
        }
      }
  }

  private def verifyEmailStatus(emailVerificationEisReturnUrl: String)
                               (implicit request: Request[AnyContent], user: TAVCUser): Future[(String, String)] ={
    val contactDetails = for {
      contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    } yield if (contactDetails.isDefined) contactDetails.get.email else ""

    val verifyStatus = for {
      email <- contactDetails
      isVerified <- emailVerificationService.verifyEmailAddress(email)
    } yield (email, isVerified)

    val result = verifyStatus.flatMap {
      case (_, Some(true)) => Future {
        ("", Constants.EmailVerified)
      }
      case (data, Some(false)) => {
        emailVerificationService.sendVerificationLink(data, emailVerificationEisReturnUrl,
          applicationConfig.emailVerificationTemplate).map {
          case true => (data, Constants.EmailNotVerified)
          case false => ("", Constants.EmailVerified)
        }
      }
      case (_, None) => Future {
        ("", "")
      }
    }
    result
  }

  private def processSendEmailVerification(result: Future[(String, String)])
                                          (implicit request: Request[AnyContent]): Future[Result] ={
    result.flatMap {
      case (_,Constants.EmailVerified) => Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
      case (data,Constants.EmailNotVerified) => Future.successful(Ok(EmailVerification(EmailVerificationModel(data))))
      case _ => Future.successful(InternalServerError(FrontendGlobal.internalServerErrorTemplate))
    }
  }
  private def processSubmitEmailVerification(result: Future[(String, String)])
                                          (implicit request: Request[AnyContent]): Future[Result] ={
    result.flatMap {
      case (_,Constants.EmailVerified) => Future.successful(Redirect(routes.CheckAnswersController.show()))
      case (data,Constants.EmailNotVerified) => Future.successful(Ok(EmailVerification(EmailVerificationModel(data))))
      case _ => Future.successful(InternalServerError(FrontendGlobal.internalServerErrorTemplate))
    }
  }
}
