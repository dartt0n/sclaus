import sbt._

object Dependencies {

  lazy val munit = "org.scalameta" %% "munit" % "1.0.0" % Test

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.5.7"

  lazy val all = Seq(catsEffect, munit)

}
