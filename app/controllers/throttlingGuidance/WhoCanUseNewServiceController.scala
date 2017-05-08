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

package controllers.throttlingGuidance

import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Action

import scala.concurrent.Future

object WhoCanUseNewServiceController extends WhoCanUseNewServiceController

  trait WhoCanUseNewServiceController extends FrontendController  {

    val show = Action.async{
      implicit request =>Future.successful(Ok(views.html.throttlingGuidance.WhoCanUseNewService()))
    }

    // link to first time using this service page once created.
    val submit = Action.async{
      implicit request =>
        Future.successful(Redirect(controllers.throttlingGuidance.routes.WhoCanUseNewServiceController.show()))
    }
}