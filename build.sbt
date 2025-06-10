ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "computational_physics"
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.12.0",
  "org.scalafx" %% "scalafx" % "22.0.0-R33",
  "org.openjfx" % "javafx-base" % "22.0.1",
  "org.openjfx" % "javafx-controls" % "23.0.1",
  "org.openjfx" % "javafx-graphics" % "23.0.1",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
)