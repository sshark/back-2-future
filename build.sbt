name := """Back to the future"""

scalaVersion := "2.12.0"

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation")

libraryDependencies += "io.javaslang" % "javaslang" % "2.0.3"

fork in run := true
