import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object FrontendBuild extends Build with MicroService {

  val appName = "investment-tax-relief-submission-frontend"
  import sbt.Keys._

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  override lazy val playSettings : Seq[Setting[_]] = Seq(
    dependencyOverrides += "org.scala-lang" % "scala-library" % "2.11.7",
    dependencyOverrides += "uk.gov.hmrc" %% "domain" % "3.5.0",
    dependencyOverrides += "uk.gov.hmrc" %% "crypto" % "3.0.0",
    dependencyOverrides += "uk.gov.hmrc" %% "secure" % "7.0.0",
    dependencyOverrides += "uk.gov.hmrc" % "play-json-logger_2.11" % "2.1.1",
    dependencyOverrides += "io.netty" % "netty" % "3.9.8.Final",
    dependencyOverrides += "com.typesafe.play" % "twirl-api_2.11" % "1.1.1",
    dependencyOverrides += "org.hamcrest" % "hamcrest-core" % "1.3",
    dependencyOverrides += "com.google.guava" % "guava" % "16.0.1"

  )
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"    
  private val playJsonLoggerVersion = "2.1.1"      
  private val frontendBootstrapVersion = "6.5.0"
  private val govukTemplateVersion = "4.0.0"
  private val playUiVersion = "4.14.0"
  private val playPartialsVersion = "4.2.0"
  private val playAuthorisedFrontendVersion = "5.0.0"
  private val playConfigVersion = "2.0.1"
  private val hmrcTestVersion = "1.6.0"
  private val cachingClientVersion = "5.3.0"
  private val mongoCachingVersion = "3.2.0"
  private val metricsPlayVersion = "2.3.0_0.1.8"
  private val metricsGrpahiteVersion = "3.0.2"
  private val playGrpahiteVersion = "2.0.0"


  
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "com.kenshoo" %% "metrics-play" % metricsPlayVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsGrpahiteVersion,
    "uk.gov.hmrc" %% "play-graphite" % playGrpahiteVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.scalatestplus" %% "play" % "1.2.0" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.8.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


