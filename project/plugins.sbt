// versions
val crossVer = "0.6.1"
val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION"))
  .filter(_.nonEmpty)
  .getOrElse("0.6.29")
val scalaNativeVersion = Option(System.getenv("SCALANATIVE_VERSION"))
  .filter(_.nonEmpty)
  .getOrElse("0.3.9")

// includes sbt-dynver sbt-pgp sbt-sonatype sbt-git
addSbtPlugin("com.geirsson" % "sbt-ci-release"  % "1.4.31")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.6.1")

// Scala Native support
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % scalaNativeVersion)

// Scala.js support
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % crossVer)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % scalaJSVersion)

// Dotty - Scala 3
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.3.4")
