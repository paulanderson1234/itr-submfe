import sbt._


object FrontendBuild extends Build with MicroService {

 val appName = "investment-tax-relief-submission-frontend"


  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "2.0.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val frontendBootstrapVersion = "7.10.0"
  private val govukTemplateVersion = "5.0.0"
  private val playUiVersion = "5.4.0"
  private val playPartialsVersion = "5.2.0"
  private val playAuthorisedFrontendVersion = "6.2.0"
  private val playConfigVersion = "3.0.0"
  private val hmrcTestVersion = "2.2.0"
  private val cachingClientVersion = "6.1.0"
  private val mongoCachingVersion = "3.2.0"
  private val playConditionalMappingVersion = "0.2.0"
  private val scalaTestVersion = "2.2.6"
  private val scalaTestPlusVersion = "1.5.1"
  private val pegDownVersion = "1.6.0"
  private val jSoupVersion = "1.8.3"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalMappingVersion)

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.pegdown" % "pegdown" % pegDownVersion % scope,
        "org.jsoup" % "jsoup" % jSoupVersion % scope,
        "org.mockito" % "mockito-all" % "1.9.5" % "optional",
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
