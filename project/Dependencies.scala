import sbt._

object Dependencies {

  lazy val zio   = "dev.zio"       %% "zio"   % "2.1.13"
  lazy val munit = "org.scalameta" %% "munit" % "1.0.0" % Test

  lazy val all = Seq(zio, munit)

}
