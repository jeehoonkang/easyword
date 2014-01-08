import play.Project._

name := """easyword"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)

playScalaSettings
