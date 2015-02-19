import com.github.retronym.SbtOneJar._

oneJarSettings

name := "scalajs-converter"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

libraryDependencies += "com.google.guava" % "guava" % "18.0"

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.10"

libraryDependencies += "org.mozilla" % "rhino" % "1.7R4"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3"

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

mainClass in oneJar := Some("io.megl.scalajs.HTML2SJS")