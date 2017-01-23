import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import wartremover._
import play.sbt.routes.RoutesKeys.routesGenerator

// imports for Asset Pipeline
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import uk.gov.hmrc.versioning.SbtGitVersioning

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq()
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models/.data/..*;iht.view.*;iht.utils.ApplicationKickOutHelper;iht.models.*;iht.forms.*;iht.config.*;iht.constants.*;.*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;iht.connector.*;iht.controllers.wraith.*;iht.controllers.testonly.*;wraith.Routes;taxreturn.Routes;registration.Routes;iht.auth.*;iht.controllers.auth.*;iht.utils.DateFields;iht.utils.DateFormatSymbols;iht.controllers.IhtConnectors;iht.controllers.application.AssetsController;iht.controllers.application.LiabilitiesController",
      ScoverageKeys.coverageMinimum := 80,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
  }


  val wartRemovedExcludedClasses = Seq()

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins : _*)
    .settings(playSettings ++ scoverageSettings : _*)
//    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(
      libraryDependencies ++= appDependencies,
      fork in Test := false,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := StaticRoutesGenerator
  )
    .settings(
      // concatenate js
      Concat.groups := Seq(
        "javascripts/iht-app.js" -> group(Seq("javascripts/show-hide-content.js", "javascripts/iht.js"))
      ),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      UglifyKeys.compressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      // below line required to force asset pipeline to operate in dev rather than only prod
      pipelineStages in Assets := Seq(concat,uglify),
      // only compress files generated by concat
      includeFilter in uglify := GlobFilter("iht-*.js")
    )
    .settings(wartremoverSettings : _*)
    .settings(
      wartremoverWarnings ++= Warts.unsafe,
      wartremoverExcluded ++= wartRemovedExcludedClasses,
      wartremoverExcluded ++= WartRemoverConfig.makeExcludedFiles(baseDirectory.value))
    .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
}

private object WartRemoverConfig{

  def findSbtFiles(rootDir: File): Seq[String] = {
    if(rootDir.getName == "project") {
      rootDir.listFiles().map(_.getName).toSeq
    } else {
      Seq()
    }
  }

  def findPlayConfFiles(rootDir: File): Seq[String] = {
    Option { new File(rootDir, "conf").listFiles() }.fold(Seq[String]()) { confFiles =>
      confFiles
        .map(_.getName.replace(".routes", ".Routes"))
    }
  }

  def makeExcludedFiles(rootDir:File):Seq[String] = {
    val excluded = findPlayConfFiles(rootDir) ++ findSbtFiles(rootDir)
    println(s"[auto-code-review] excluding the following files: ${excluded.mkString(",")}")
    excluded
  }
}
