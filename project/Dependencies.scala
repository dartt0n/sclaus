import sbt._

object Dependencies {

  lazy val all = Seq(
    "io.github.apimorphism"  %% "telegramium-core" % "9.801.0",
    "io.github.apimorphism"  %% "telegramium-high" % "9.801.0",
    "org.typelevel"          %% "cats-effect"      % "3.5.7",
    "org.scalameta"          %% "munit"            % "1.0.0" % Test,
    "com.github.nscala-time" %% "nscala-time"      % "2.34.0",
    // "io.circe"              %% "circe-core"       % "0.14.10",
    // "io.circe"              %% "circe-generic"    % "0.14.10",
    // "io.circe"              %% "circe-parser"     % "0.14.10",
    "org.tpolecat" %% "doobie-core"     % "1.0.0-RC5",
    "org.tpolecat" %% "doobie-hikari"   % "1.0.0-RC5",
    "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC5",
  )

}
