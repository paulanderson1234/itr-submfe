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

package controllers.feedback

import auth.{MockAuthConnector, MockConfig}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.mvc.{AnyContent, Request, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, FormPartialRetriever, HtmlPartial}

import scala.concurrent.Future

class FeedbackControllerSpec extends BaseSpec {

  val mockHttp = mock[WSHttp]

  object TestController extends FeedbackController {
    override implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever = new CachedStaticHtmlPartialRetriever {
      override def httpGet: HttpGet = ???

      override def getPartialContent(url: String, templateParameters: Map[String, String], errorMessage: Html)(implicit request: RequestHeader): Html =
        Html("")
    }
    override implicit val formPartialRetriever: FormPartialRetriever = new FormPartialRetriever {
      override def crypto: (String) => String = ???

      override def httpGet: HttpGet = ???

      override def getPartialContent(url: String, templateParameters: Map[String, String], errorMessage: Html)(implicit request: RequestHeader): Html = Html("")
    }

    protected def loadPartial(url: String)(implicit request: RequestHeader): HtmlPartial = ???
    override def httpPost: HttpPost = mockHttp
    override def localSubmitUrl(implicit request: Request[AnyContent]): String = ""
    override def contactFormReferer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")

    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
    override val acceptedFlows = Seq()
  }

  def setupMocks(status: Int = Status.OK, response: Option[String] = None): Unit =
    when(mockHttp.POSTForm[HttpResponse](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, responseString = response)))

  "GET /feedback" should {
    "return feedback page" in {
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe Status.OK
      )
    }
  }

  "POST /feedback" should {
    "return form with thank you for valid selections" in {
      setupMocks(response = Some("1234"))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => redirectLocation(result) shouldBe Some(routes.FeedbackController.thankyou().url)
      )
    }

    "return form with errors for invalid selections" in {
      setupMocks(Status.BAD_REQUEST, Some("<p>:^(</p>"))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => status(result) shouldBe Status.BAD_REQUEST
      )
    }

    "return error for other http code back from contact-frontend" in {
      setupMocks(FORBIDDEN)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      )
    }

    "return internal server error when there is an empty form" in {
      setupMocks(response = Some("1234"))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.submit)(
        result => status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      )
    }
  }

  "GET /feedback/thankyou" should {
    "should return the thank you page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.thankyou)(
        result => status(result) shouldBe Status.OK
      )
    }
  }
}
