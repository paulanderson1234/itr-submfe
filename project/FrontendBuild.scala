import sbt._


object FrontendBuild extends Build with MicroService {

 val appName = "investment-tax-relief-submission-frontend"


  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val frontendBootstrapVersion = "8.10.0"
  private val playPartialsVersion = "6.1.0"
  private val hmrcTestVersion = "2.3.0"
  private val cachingClientVersion = "7.0.0"
  private val mongoCachingVersion = "5.2.0"
  private val playConditionalMappingVersion = "0.2.0"
  private val scalaTestVersion = "2.2.6"
  private val scalaTestPlusVersion = "1.5.1"
  private val pegDownVersion = "1.6.0"
  private val jSoupVersion = "1.8.3"
  private val mockitoAll = "1.9.5"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
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
        "org.mockito" % "mockito-all" % mockitoAll % "optional",
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
