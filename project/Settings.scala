import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */
object Settings {
  /** The name of your application */
  val name = "scalajs-converter"

  /** The version of your application */
  val version = "1.0.2"

  /** Options for the scala compiler */
  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Set some basic options when running the project with Revolver */
  val jvmRuntimeOptions = Seq(
    "-Xmx1G"
  )
  /**
   * These dependencies are shared between JS and JVM projects
   * the special %%% function selects the correct version for each project
   */
  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % versions.autowire,
    "io.circe" %%% "circe-core" % versions.circe,
    "io.circe" %%% "circe-generic" % versions.circe,
    "io.circe" %%% "circe-parser" % versions.circe,
    "io.circe" %%% "circe-java8" % versions.circe,
    "com.lihaoyi" %%% "utest" % versions.uTest
  ))
  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "com.google.guava" % "guava" % "23.0",
    "com.typesafe.play" %% "play-json" % "2.6.9",
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.22",
    "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
//    "org.specs2" %% "specs2" % "3.8.9" % Test,
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    "org.mozilla" % "rhino" % versions.rhino//,
//    "org.webjars" % "font-awesome" % "4.7.0",
//    "org.webjars" % "bootstrap" % versions.bootstrap
  ))
  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,
    "com.lihaoyi" %%% "scalarx" % versions.scalaRx
  ))
  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars" % "react" % versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "chartjs" % versions.chartjs / "Chart.js" minified "Chart.min.js",
    "org.webjars" % "log4javascript" % versions.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js"
  ))

  import versions._
  val clientNpmDependences = Seq(
    "react" -> react,
    "react-dom" -> react,
    "react-addons-test-utils" -> reactTestUtils,
    "log4javascript" -> log4Javascript,
    "bootstrap" -> bootstrap,
    "react-handsontable" -> reactHandsontable
  )

  val clientNpmDevDependencies = Seq(
    "webpack" -> versions.webpackVersion,
    "webpack-dev-server" -> versions.webpackDevVersion,
    "css-loader" -> "1.0.0",
    "react-hot-loader" -> "4.0.0",
    "style-loader" -> "0.21.0",
    "expose-loader" -> exposeLoader,
    "url-loader" -> "1.0.1",
    "extract-text-webpack-plugin" -> "4.0.0-beta.0",
    "file-loader" -> "1.1.11",
    "image-webpack-loader" -> "4.2.0",
    "imagemin" -> "5.3.1",
    "less" -> "3.0.1",
    "less-loader" -> "4.1.0",
    "lodash" -> "4.17.5",
    "node-libs-browser" -> "2.1.0",
    "@types/webpack" -> "4.1.0"
  )


  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val scala = "2.12.6"
    val scalaDom = "0.9.6"
    val scalajsReact = "1.2.3"
    val scalajsReactComponents = "0.8.0"
    val reactTestUtils = "15.4.1"
    val exposeLoader = "0.7.1"
    val log4Javascript = "1.4.15"
    val reactHandsontable = "0.3.1"
    val scalaCSS = "0.5.5"
    val scalaRx = "0.3.2"
    val log4js = "1.4.10"
    val autowire = "0.2.6"
    val booPickle = "1.3.0"
    val uTest = "0.6.3"
    val circe = "0.9.3"

    val react = "16.3.2"
    val jQuery = "1.11.1"
    val bootstrap = "4.1.3"
    val chartjs = "1.0.2"
    val rhino = "1.7.10"
    lazy val webpackVersion = "4.1.1"
    lazy val webpackDevVersion = "3.1.5"

    val playScripts = "0.5.0"
  }
}
