name := """contributions-frontend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"
val scalaUri = "com.netaporter" %% "scala-uri" % "0.4.6"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.1"
val membershipCommon = "com.gu" %% "membership-common" % "0.225"
val sentryRavenLogback = "com.getsentry.raven" % "raven-logback" % "7.2.3"
val memsubCommonPlayAuth = "com.gu" %% "memsub-common-play-auth" % "0.7"
libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    scalaUri,
    scalaz,
    membershipCommon,
    sentryRavenLogback,
    memsubCommonPlayAuth,
    "com.softwaremill.macwire" %% "macros" % "2.2.2" % "provided",
    "com.softwaremill.macwire" %% "util" % "2.2.2",
    "com.softwaremill.macwire" %% "proxy" % "2.2.2"
)
dependencyOverrides += "com.typesafe.play" %% "play-json" % "2.4.6"


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

addCommandAlias("devrun", "run -Dconfig.resource=dev.conf 9111")
