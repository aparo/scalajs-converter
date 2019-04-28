// repository for Typesafe plugins
//resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "0.6.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.20")

addSbtPlugin("com.typesafe.play"         % "sbt-plugin"                % "2.7.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")
// addSbtPlugin("com.typesafe.sbteclipse"   % "sbteclipse-plugin"         % "5.2.4")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6")

addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.13.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.3")