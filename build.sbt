import Dependencies._

val projectVersion = "0.0.0"
val projectName    = "sclaus"

Compile / scalacOptions ++= Seq(
  "-Xkind-projector:underscores",
  // "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wshadow:all",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:implicitConversions",
  "-Wnonunit-statement",
)

lazy val app = project
  .in(file("."))
  .settings(
    name         := projectName,
    version      := projectVersion,
    scalaVersion := "3.5.2",
    libraryDependencies ++= Dependencies.all,
    assembly / mainClass          := Some("com.dartt0n.sclaus.Main"),
    assembly / assemblyOutputPath := file("target/assembly/sclaus.jar"),
  )

enablePlugins(JavaAppPackaging)
