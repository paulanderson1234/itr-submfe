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

package services

import auth.TAVCUser
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{EmailConfirmationConnector, S4LConnector}
import models.registration.RegistrationDetailsModel
import models.{ContactDetailsModel, EmailConfirmationModel}
import models.submission.SubmissionResponse
import play.api.Logger
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import play.mvc.Http.Status._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait EmailConfirmationService {

  val s4LConnector: S4LConnector
  val emailTemplate: String
  val registrationDetailsService: RegistrationDetailsService
  val emailConfirmationConnector: EmailConfirmationConnector

  def sendEmailConfirmation(tavcRef: String, submissionResponse: SubmissionResponse)(implicit hc: HeaderCarrier, user: TAVCUser): Future[HttpResponse] = {
    (for {
      registrationDetailsModel <- registrationDetailsService.getRegistrationDetails(tavcRef)
      emailConfirmationModel <- getEmailConfirmationModel(registrationDetailsModel.get, submissionResponse.processingDate, submissionResponse.formBundleNumber)
      response  <- emailConfirmationConnector.sendEmailConfirmation(emailConfirmationModel.get)
    }yield {
      Logger.info(s"[EmailConfirmationService][sendEmailConfirmation] - Response code for sending confirmation email: ${response.status}")
      response
    }).recover{
      case throwable => {
        Logger.info(s"[EmailConfirmationService][sendEmailConfirmation] - Failed to send email confirmation: ${throwable.getMessage}")
        HttpResponse(INTERNAL_SERVER_ERROR)
      }
    }
  }

  private def getEmailConfirmationModel(registrationDetailsModel: RegistrationDetailsModel, date: String, formBundleReferenceNumber: String)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[EmailConfirmationModel]] = {
    (for{
      contactDetails <- s4LConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    }yield Some(EmailConfirmationModel(Array(contactDetails.get.email),
                                  emailTemplate,
                                  EmailConfirmationModel.parameters(registrationDetailsModel.organisationName, formBundleReferenceNumber)))).recover{
      case throwable => {
        Logger.warn(s"[EmailConfirmationService][getEmailConfirmationModel] - Failed to build EmailConfirmationModel:  ${throwable.getMessage}")
        None
      }
    }
  }

}

object EmailConfirmationService extends EmailConfirmationService {
  val s4LConnector: S4LConnector = S4LConnector
  val emailTemplate = FrontendAppConfig.emailTemplate
  val registrationDetailsService = RegistrationDetailsService
  val emailConfirmationConnector = EmailConfirmationConnector
}
