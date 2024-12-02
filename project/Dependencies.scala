import sbt._

object Dependencies {

  lazy val munit = "org.scalameta" %% "munit" % "1.0.0" % Test

  lazy val catsEffect      = "org.typelevel"         %% "cats-effect"      % "3.5.7"
  lazy val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % "9.800.0"
  lazy val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % "9.800.0"

  lazy val all = Seq(catsEffect, munit, telegramiumCore, telegramiumHigh)

}
