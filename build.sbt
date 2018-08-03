import sbt.Keys._
import sbt.Project.projectToRef
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedDependencies.value
  )

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions ++ Seq("-P:scalajs:suppressExportDeprecations"),
    libraryDependencies ++= Settings.scalajsDependencies.value,
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("converter.client.ScalaJSConverter"),
      webpackBundlingMode := BundlingMode.LibraryOnly(),
      version in webpack := Settings.versions.webpackVersion,
    version in startWebpackDevServer := Settings.versions.webpackDevVersion,
      // by default we do development build, no eliding
      elideOptions := Seq(),
      scalacOptions ++= elideOptions.value,
//      jsDependencies ++= Settings.jsDependencies.value,
      // reactjs testing
      jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
    artifactPath.in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value /
      ((moduleName in fastOptJS).value + "-opt.js")),
    scalaJSStage in Test := FastOptStage,

      // 'new style js dependencies with scalaBundler'
      npmDependencies in Compile ++= Settings.clientNpmDependences,
      npmDevDependencies in Compile ++= Settings.clientNpmDevDependencies,
      // RuntimeDOM is needed for tests
      jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
      useYarn := true,
      // yes, we want to package JS dependencies
      skip in packageJSDependencies := false,
    webpackConfigFile in (Test) := Some(baseDirectory.value / "webpack.config.test.js"),
    webpackConfigFile in(Compile, fastOptJS) := Some(baseDirectory.value / "webpack.config.dev.js"),
    webpackConfigFile in(Compile, fullOptJS) := Some(baseDirectory.value / "webpack.config.prod.js"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
      resolvers += Resolver.defaultLocal,
      // use uTest framework for tests
      testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSWeb)
  .dependsOn(sharedJS)

// Client projects (just one in this case)
lazy val clients = Seq(client)

lazy val converterLibrary = (project in file("html2sjs"))
  .settings(
    name := "html2sjs",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmLibraryDependencies.value,
  )
  .dependsOn(sharedJVM)

// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
      libraryDependencies += guice,
    commands += ReleaseCmd,
    // connect to the client project
    scalaJSProjects := clients,
      pipelineStages in Assets := Seq(scalaJSPipeline, digest, gzip),
      // triggers scalaJSPipeline when using compile or continuous compilation
//      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    // compress CSS
    LessKeys.compress in Assets := true
  )
  .enablePlugins(PlayScala)
  .enablePlugins(WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(converterLibrary)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "server/clean" ::
    "server/test" ::
    "server/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}

// lazy val root = (project in file(".")).aggregate(client, server)

// loads the Play server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
