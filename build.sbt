import play.Project._

name := """easyword"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "joda-time" % "joda-time" % "2.3"
)

playScalaSettings
