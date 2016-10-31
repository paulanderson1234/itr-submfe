[1mdiff --git a/app/connectors/SubscriptionConnector.scala b/app/connectors/SubscriptionConnector.scala[m
[1mindex 26559d7..7a7c372 100644[m
[1m--- a/app/connectors/SubscriptionConnector.scala[m
[1m+++ b/app/connectors/SubscriptionConnector.scala[m
[36m@@ -25,56 +25,16 @@[m [mimport uk.gov.hmrc.play.http._[m
 [m
 import scala.concurrent.Future[m
 [m
[31m-object SubmissionConnector extends SubmissionConnector with ServicesConfig {[m
[31m-  val serviceUrl = baseUrl("investment-tax-relief-submission")[m
[32m+[m[32mobject SubscriptionConnector extends SubscriptionConnector with ServicesConfig {[m
[32m+[m[32m  val serviceUrl = baseUrl("investment-tax-relief-subscription")[m
   val http = WSHttp[m
 }[m
 [m
[31m-trait SubmissionConnector {[m
[32m+[m[32mtrait SubscriptionConnector {[m
   val serviceUrl: String[m
   val http: HttpGet with HttpPost with HttpPut[m
 [m
[31m-  def validateKiCostConditions(operatingCostYear1: Int, operatingCostYear2: Int, operatingCostYear3: Int,[m
[31m-                               rAndDCostsYear1: Int, rAndDCostsYear2: Int, rAndDCostsYear3: Int)[m
[31m-                              (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {[m
[31m-[m
[31m-    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-ki-costs/" +[m
[31m-      s"operating-costs/$operatingCostYear1/$operatingCostYear2/$operatingCostYear3/" +[m
[31m-      s"rd-costs/$rAndDCostsYear1/$rAndDCostsYear2/$rAndDCostsYear3")[m
[31m-  }[m
[31m-[m
[31m-  def validateSecondaryKiConditions(hasPercentageWithMasters: Boolean,[m
[31m-                                    hasTenYearPlan: Boolean)[m
[31m-                                   (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {[m
[31m-[m
[31m-    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-secondary-conditions/has-percentage-with-masters/" +[m
[31m-      s"$hasPercentageWithMasters/has-ten-year-plan/$hasTenYearPlan")[m
[31m-  }[m
[31m-[m
[31m-  def checkLifetimeAllowanceExceeded(hadPrevRFI: Boolean, isKi: Boolean, previousInvestmentSchemesTotal: Int,[m
[31m-                                     proposedAmount: Int)[m
[31m-                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {[m
[31m-[m
[31m-    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/lifetime-allowance/lifetime-allowance-checker/had-previous-rfi/" +[m
[31m-      s"$hadPrevRFI/is-knowledge-intensive/$isKi/previous-schemes-total/$previousInvestmentSchemesTotal/proposed-amount/$proposedAmount")[m
[31m-[m
[31m-  }[m
[31m-[m
[31m-  def checkAveragedAnnualTurnover(proposedInvestmentAmount: ProposedInvestmentModel, annualTurnoverCostsModel: AnnualTurnoverCostsModel)[m
[31m-                                 (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {[m
[31m-    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/averaged-annual-turnover/check-averaged-annual-turnover/" +[m
[31m-      s"proposed-investment-amount/${proposedInvestmentAmount.investmentAmount}/annual-turn-over/${annualTurnoverCostsModel.amount1}" +[m
[31m-      s"/${annualTurnoverCostsModel.amount2}/${annualTurnoverCostsModel.amount3}/${annualTurnoverCostsModel.amount4}/${annualTurnoverCostsModel.amount5}")[m
[31m-  }[m
[31m-[m
[31m-  //TODO: put all these methods in a service?[m
[31m-  def submitAdvancedAssurance(submissionRequest: Submission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {[m
[31m-    val tavcReferenceId = "XADD00000001234" //TODO: get from enrolment[m
[31m-    val json = Json.toJson(submissionRequest)[m
[31m-[m
[31m-    val targetSubmissionModel = Json.parse(json.toString()).as[DesSubmitAdvancedAssuranceModel][m
[31m-[m
[31m-    http.POST[JsValue, HttpResponse](s"$serviceUrl/investment-tax-relief/advanced-assurance/$tavcReferenceId/submit", Json.toJson(targetSubmissionModel))[m
[31m-  }[m
[32m+[m[32m  def getSubscriptionDetails(tavcReferenceNumber: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =[m
[32m+[m[32m    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief-subscription/$tavcReferenceNumber/subscription")[m
 [m
 }[m
[1mdiff --git a/test/connectors/SubscriptionConnectorSpec.scala b/test/connectors/SubscriptionConnectorSpec.scala[m
[1mindex f94e4cb..b2e59f7 100644[m
[1m--- a/test/connectors/SubscriptionConnectorSpec.scala[m
[1m+++ b/test/connectors/SubscriptionConnectorSpec.scala[m
[36m@@ -32,163 +32,112 @@[m
 [m
 package connectors[m
 [m
[31m-import java.util.UUID[m
[31m-[m
[31m-import models.{AnnualTurnoverCostsModel, ProposedInvestmentModel}[m
[31m-import models.submission.{AnnualCostModel, TurnoverCostModel}[m
[31m-import play.api.test.Helpers._[m
[32m+[m[32mimport controllers.helpers.FakeRequestHelper[m
 import fixtures.SubmissionFixture[m
[31m-[m
[31m-import org.scalatest.BeforeAndAfterEach[m
[31m-import org.scalatestplus.play.OneServerPerSuite[m
 import org.mockito.Matchers[m
 import org.mockito.Mockito._[m
[32m+[m[32mimport org.mockito.stubbing.OngoingStubbing[m
[32m+[m[32mimport org.scalatest.BeforeAndAfterEach[m
 import org.scalatest.mock.MockitoSugar[m
[31m-import play.api.libs.json.JsValue[m
[32m+[m[32mimport org.scalatestplus.play.OneServerPerSuite[m
[32m+[m[32mimport play.api.http.Status[m
[32m+[m[32mimport play.api.libs.json.Json[m
 import uk.gov.hmrc.play.frontend.controller.FrontendController[m
[31m-import uk.gov.hmrc.play.http.ws.WSHttp[m
 import uk.gov.hmrc.play.http._[m
 import uk.gov.hmrc.play.http.logging.SessionId[m
[32m+[m[32mimport uk.gov.hmrc.play.http.ws.WSHttp[m
 import uk.gov.hmrc.play.test.UnitSpec[m
 [m
 import scala.concurrent.Future[m
 [m
 class SubscriptionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with SubmissionFixture {[m
 [m
[31m-  val mockHttp : WSHttp = mock[WSHttp][m
[31m-  val sessionId = UUID.randomUUID.toString[m
[31m-[m
[31m-  object TargetSubmissionConnector extends SubmissionConnector with FrontendController {[m
[31m-    override val serviceUrl = "dummy"[m
[31m-    override val http = mockHttp[m
[31m-  }[m
[31m-[m
[31m-  val validResponse = true[m
[31m-  val trueResponse = true[m
[31m-  val falseResponse = false[m
[31m-[m
[31m-  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))[m
[31m-[m
[31m-  "Calling validateKiCostConditions" should {[m
[31m-[m
[31m-    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))[m
[31m-[m
[31m-    "return a valid response" in {[m
[31m-[m
[31m-      val operatingCostData: Int = 1000[m
[31m-      val rAndDCostData: Int = 100[m
[31m-[m
[31m-      val result = TargetSubmissionConnector.validateKiCostConditions(operatingCostData,operatingCostData,[m
[31m-        operatingCostData,rAndDCostData,rAndDCostData,rAndDCostData)[m
[31m-      await(result) shouldBe Some(trueResponse)[m
[31m-    }[m
[31m-  }[m
[31m-[m
[31m-  "Calling validateSecondaryKiConditions" should {[m
[31m-[m
[31m-    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))[m
[31m-[m
[31m-    "return a valid response" in {[m
[31m-[m
[31m-      val hasPercentageWithMasters: Boolean = true[m
[31m-      val hasTenYearPlan: Boolean = true[m
[31m-[m
[31m-      val result = TargetSubmissionConnector.validateSecondaryKiConditions(hasPercentageWithMasters,hasTenYearPlan)[m
[31m-      await(result) shouldBe Some(trueResponse)[m
[31m-    }[m
[31m-  }[m
[31m-[m
[31m-  "Calling checkLifetimeAllowanceExceeded" should {[m
[31m-[m
[31m-    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))[m
[31m-[m
[31m-    "return a valid response" in {[m
[31m-[m
[31m-      val hadPrevRFI = true[m
[31m-      val isKi = true[m
[31m-      val previousInvestmentSchemesTotal= 1000[m
[31m-      val proposedAmount = 1000[m
[31m-[m
[31m-      val result = TargetSubmissionConnector.checkLifetimeAllowanceExceeded(hadPrevRFI, isKi, previousInvestmentSchemesTotal, proposedAmount)[m
[31m-      await(result) shouldBe Some(validResponse)[m
[31m-    }[m
[31m-  }[m
[31m-[m
[31m-[m
[31m-  "Calling checkAveragedAnnualTurnover" should {[m
[31m-[m
[31m-    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))[m
[31m-[m
[31m-    "return a valid response" in {[m
[31m-[m
[31m-      val proposedInvestment = ProposedInvestmentModel(50)[m
[31m-      val annualTurnoverCosts = AnnualTurnoverCostsModel("100","100","100","100","100")[m
[31m-[m
[31m-      val result = TargetSubmissionConnector.checkAveragedAnnualTurnover(proposedInvestment,annualTurnoverCosts)[m
[31m-      await(result) shouldBe Some(validResponse)[m
[31m-    }[m
[31m-  }[m
[31m-[m
[31m-  "Calling submitAdvancedAssurance with a email with a valid model" should {[m
[31m-[m
[31m-    "return a OK" in {[m
[31m-[m
[31m-      val validRequest = fullSubmissionSourceData[m
[31m-      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))[m
[31m-        .thenReturn(Future.successful(HttpResponse(OK)))[m
[31m-      val result = TargetSubmissionConnector.submitAdvancedAssurance(validRequest)[m
[31m-      await(result).status shouldBe OK[m
[31m-    }[m
[31m-  }[m
[31m-[m
[31m-  "Calling submitAdvancedAssurance with a email containing 'badrequest'" should {[m
[31m-[m
[31m-    "return a BAD_REQUEST error" in {[m
[31m-[m
[31m-      val request = fullSubmissionSourceData[m
[31m-      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))[m
[31m-        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))[m
[31m-      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)[m
[31m-      await(result).status shouldBe BAD_REQUEST[m
[31m-    }[m
[32m+[m[32m  object TargetSubscriptionConnector extends SubscriptionConnector with FrontendController with FakeRequestHelper {[m
[32m+[m[32m    override val serviceUrl = "host"[m
[32m+[m[32m    override val http = mock[WSHttp][m
   }[m
 [m
[31m-[m
[31m-  "Calling submitAdvancedAssurance with a email containing 'forbidden'" should {[m
[31m-[m
[31m-    "return a FORBIDDEN Error" in {[m
[31m-[m
[31m-      val request = fullSubmissionSourceData[m
[31m-      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))[m
[31m-        .thenReturn(Future.successful(HttpResponse(FORBIDDEN)))[m
[31m-      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)[m
[31m-      await(result).status shouldBe FORBIDDEN[m
[31m-    }[m
[32m+[m[32m  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))[m
[32m+[m[32m  val validTavcReference = "XATAVC000123456"[m
[32m+[m
[32m+[m[32m  val successResponse = HttpResponse(Status.OK, responseJson = Some(Json.parse([m
[32m+[m[32m    """[m
[32m+[m[32m      |{[m
[32m+[m[32m      |    "processingDate": "2001-12-17T09:30:47Z",[m
[32m+[m[32m      |    "subscriptionType": {[m
[32m+[m[32m      |        "safeId": "XA0000000012345",[m
[32m+[m[32m      |        "correspondenceDetails": {[m
[32m+[m[32m      |            "contactName": {[m
[32m+[m[32m      |                "name1": "John",[m
[32m+[m[32m      |                "name2": "Brown"[m
[32m+[m[32m      |            },[m
[32m+[m[32m      |            "contactDetails": {[m
[32m+[m[32m      |                "phoneNumber": "0000 10000",[m
[32m+[m[32m      |                "mobileNumber": "0000 2000",[m
[32m+[m[32m      |                "faxNumber": "0000 30000",[m
[32m+[m[32m      |                "emailAddress": "john.smith@noplace.atall.com"[m
[32m+[m[32m      |            },[m
[32m+[m[32m      |            "contactAddress": {[m
[32m+[m[32m      |                "addressLine1": "12 some street",[m
[32m+[m[32m      |                "addressLine2": "some line 2",[m
[32m+[m[32m      |                "addressLine3": "some line 3",[m
[32m+[m[32m      |                "addressLine4": "some line 4",[m
[32m+[m[32m      |                "countryCode": "GB",[m
[32m+[m[32m      |                "postalCode": "AA1 1AA"[m
[32m+[m[32m      |            }[m
[32m+[m[32m      |        }[m
[32m+[m[32m      |    }[m
[32m+[m[32m      |}[m
[32m+[m[32m    """.stripMargin[m
[32m+[m[32m  )))[m
[32m+[m
[32m+[m[32m  val failedResponse = HttpResponse(Status.BAD_REQUEST, responseJson = Some(Json.parse([m
[32m+[m[32m    """[m
[32m+[m[32m      |{[m
[32m+[m[32m      |    "reason": {[m
[32m+[m[32m      |        "type": "Bad Request",[m
[32m+[m[32m      |        "description": "The request was invalid"[m
[32m+[m[32m      |    }[m
[32m+[m[32m      |}[m
[32m+[m[32m    """.stripMargin[m
[32m+[m[32m  )))[m
[32m+[m
[32m+[m[32m  def setupMockedResponse(data: HttpResponse): OngoingStubbing[Future[HttpResponse]] = {[m
[32m+[m[32m    when(TargetSubscriptionConnector.http.GET[HttpResponse]([m
[32m+[m[32m      Matchers.eq(s"${TargetSubscriptionConnector.serviceUrl}/investment-tax-relief-subscription/$validTavcReference/subscription"))(Matchers.any(), Matchers.any()))[m
[32m+[m[32m      .thenReturn(Future.successful(data))[m
   }[m
 [m
[32m+[m[32m  "Calling getSubscriptonDetails" when {[m
 [m
[31m-  "Calling submitAdvancedAssurance with a email containing 'serviceunavailable'" should {[m
[32m+[m[32m    "expecting a successful response" should {[m
[32m+[m[32m      lazy val result = TargetSubscriptionConnector.getSubscriptionDetails(validTavcReference)[m
[32m+[m[32m      lazy val response = await(result)[m
 [m
[31m-    "return a SERVICE UNAVAILABLE ERROR" in {[m
[32m+[m[32m      "return a Status OK (200) response" in {[m
[32m+[m[32m        setupMockedResponse(successResponse)[m
[32m+[m[32m        response.status shouldBe Status.OK[m
[32m+[m[32m      }[m
 [m
[31m-      val request = fullSubmissionSourceData[m
[31m-      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))[m
[31m-        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))[m
[31m-      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)[m
[31m-      await(result).status shouldBe SERVICE_UNAVAILABLE[m
[32m+[m[32m      "Have a successful Json Body response" in {[m
[32m+[m[32m        setupMockedResponse(successResponse)[m
[32m+[m[32m        response.json shouldBe successResponse.json[m
[32m+[m[32m      }[m
     }[m
[31m-  }[m
 [m
[31m-  "Calling submitAdvancedAssurance with a email containing 'internalservererror'" should {[m
[32m+[m[32m    "expecting a non-successful response" should {[m
[32m+[m[32m      lazy val result = TargetSubscriptionConnector.getSubscriptionDetails(validTavcReference)[m
[32m+[m[32m      lazy val response = await(result)[m
 [m
[31m-    "return a INTERNAL SERVER ERROR" in {[m
[32m+[m[32m      "return a Status OK (200) response" in {[m
[32m+[m[32m        setupMockedResponse(failedResponse)[m
[32m+[m[32m        response.status shouldBe Status.BAD_REQUEST[m
[32m+[m[32m      }[m
 [m
[31m-      val request = fullSubmissionSourceData[m
[31m-      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))[m
[31m-        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))[m
[31m-      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)[m
[31m-      await(result).status shouldBe INTERNAL_SERVER_ERROR[m
[32m+[m[32m      "Have a successful Json Body response" in {[m
[32m+[m[32m        setupMockedResponse(failedResponse)[m
[32m+[m[32m        response.json shouldBe failedResponse.json[m
[32m+[m[32m      }[m
     }[m
   }[m
[31m-[m
 }[m
