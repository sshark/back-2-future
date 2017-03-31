name := """Back to the future"""

scalaVersion := "2.12.0"

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation")

libraryDependencies += "io.javaslang" % "javaslang" % "2.0.3"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.9"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.3"

fork in run := true
