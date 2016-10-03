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

package controllers


import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import common.{Constants, KeystoreKeys}
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import models.{ContactDetailsModel, SubmissionRequest, SubmissionResponse, YourCompanyNeedModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController


object AcknowledgementController extends AcknowledgementController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AcknowledgementController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val keyStoreConnector: KeystoreConnector
  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    /** Dummy implementation. Will be replaced by final Submission model**/

    def subModel =for {
      contactDetails <- keyStoreConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
      yourCompanyNeed <- keyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](KeystoreKeys.yourCompanyNeed)
    }yield SubmissionRequest(contactDetails.get,yourCompanyNeed.get)

    val submissionResponseModel = subModel.flatMap{ model =>
      submissionConnector.submitAdvancedAssurance(model)
    }
    submissionResponseModel.map { submissionResponse =>
      submissionResponse.status match {
        case OK => Ok(views.html.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
        case _ => InternalServerError
      }
    }
  }
}
