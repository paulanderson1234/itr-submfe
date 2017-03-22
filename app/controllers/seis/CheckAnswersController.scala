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

package controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, SEIS, TAVCUser}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import controllers.predicates.FeatureSwitch
import models._
import models.seis.SEISCheckAnswersModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.seis.checkAndSubmit.CheckAnswers
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CheckAnswersController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper
  with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  def checkAnswersModel(implicit headerCarrier: HeaderCarrier, user: TAVCUser): Future[SEISCheckAnswersModel] = for {
    registeredAddress <- s4lConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    tradeStartDate <- s4lConnector.fetchAndGetFormData[TradeStartDateModel](KeystoreKeys.tradeStartDate)
    natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    subsidiaries <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
    hadPreviousRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    previousSchemes <- getAllInvestmentFromKeystore(s4lConnector)
    proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
    subsidiariesSpendingInvestment <- s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
    subsidiariesNinetyOwned <- s4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
    contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
  } yield SEISCheckAnswersModel(registeredAddress, dateOfIncorporation, tradeStartDate, natureOfBusiness, subsidiaries, hadPreviousRFI,
    previousSchemes, proposedInvestment, subsidiariesSpendingInvestment, subsidiariesNinetyOwned, contactDetails, contactAddress,
    applicationConfig.uploadFeatureEnabled)

  def show(envelopeId: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      if (envelopeId.fold("")(_.toString).length > 0) {
        s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId.getOrElse(""))
      }

      checkAnswersModel.flatMap(checkAnswers => Future.successful(Ok(CheckAnswers(checkAnswers))))
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId).flatMap {
        envelopeId => {
          if (envelopeId.isEmpty)
            Future.successful(Redirect(routes.AcknowledgementController.show()))
          else Future.successful(Redirect(controllers.seis.routes.AttachmentsAcknowledgementController.show()))

        }
      }
    }
  }

}
