import zio._
import zio.Console._

object Sclaus extends ZIOAppDefault {

  def run = for {
    _ <- printLine("Ho, ho ho! Merry Christmas! 🎅🏻")
  } yield ()

}
