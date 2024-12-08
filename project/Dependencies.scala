import sbt._

object Dependencies {

  lazy val all = Seq(
    "io.github.apimorphism"      %% "telegramium-core"      % "9.801.0",
    "io.github.apimorphism"      %% "telegramium-high"      % "9.801.0",
    "org.typelevel"              %% "cats-effect"           % "3.5.7",
    "com.github.nscala-time"     %% "nscala-time"           % "2.34.0",
    "com.typesafe"                % "config"                % "1.4.3",
    "io.circe"                   %% "circe-generic"         % "0.14.10",
    "io.circe"                   %% "circe-parser"          % "0.14.10",
    "io.circe"                   %% "circe-config"          % "0.10.1",
    "org.tpolecat"               %% "doobie-core"           % "1.0.0-RC5",
    "org.tpolecat"               %% "doobie-hikari"         % "1.0.0-RC5",
    "org.tpolecat"               %% "doobie-postgres"       % "1.0.0-RC5",
    "tf.tofu"                    %% "tofu-logging-log4cats" % "0.13.6",
    "tf.tofu"                    %% "tofu-core-ce3"         % "0.13.6",
    "org.typelevel"              %% "log4cats-slf4j"        % "2.7.0",
    "com.typesafe.scala-logging" %% "scala-logging"         % "3.9.5",
    "ch.qos.logback"              % "logback-classic"       % "1.2.10",
  )

}
