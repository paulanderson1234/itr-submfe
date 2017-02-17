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

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import models.ContactDetailsModel
import forms.ContactDetailsForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.contactInformation.ContactDetails
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ContactDetailsController extends ContactDetailsController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ContactDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))



  val show = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.manualContactDetails).map {
        case Some(data) => Ok(ContactDetails(contactDetailsForm.fill(data)))
        case None => Ok(ContactDetails(contactDetailsForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
      contactDetailsForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(ContactDetails(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.manualContactDetails, validFormData)
          s4lConnector.saveFormData(KeystoreKeys.contactDetails, validFormData)
          Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
        }
      )
    }
  }
}
