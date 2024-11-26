import Dependencies._
import sbt.Keys.libraryDependencies

Compile / scalacOptions ++= Seq(
  "-Xkind-projector:underscores",
  "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wshadow:all",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:implicitConversions"
)

lazy val root = project
  .in(file("."))
  .settings(
    name         := "sclaus",
    version      := "0.0.0",
    scalaVersion := "3.5.2",
    libraryDependencies ++= Dependencies.all
  )
