package com.dartt0n.sclaus

import cats._
// import cats.data._
import cats.effect._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.dartt0n.sclaus.repository.PostgresRepository
import com.dartt0n.sclaus.service.UserStorage
import doobie._
// import doobie.implicits._
import doobie.util.transactor.Transactor
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import telegramium.bots.high.{Api, BotApi}
import doobie.free.connection.ConnectionIO
import org.joda.time.DateTime

object Main extends IOApp {

  def funcK(transactor: Transactor[IO]): ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    def apply[A](fa: ConnectionIO[A]): IO[A] = transactor.trans.apply(fa)
  }

  def run(args: List[String]) =
    // todo: load from config
    val token = ""
    val calendar = EventCalendar(
      stage1Begin = DateTime.now().plusDays(1),
      stage1End = DateTime.now().plusDays(2),
      stage2Begin = DateTime.now().plusDays(3),
      stage2End = DateTime.now().plusDays(4),
      stage3Begin = DateTime.now().plusDays(5),
      stage3End = DateTime.now().plusDays(6),
    )

    for {
      _ <- IO.println("Ho, ho ho! Merry Christmas 🎅!")
      _ <- BlazeClientBuilder[IO].resource.use { http =>
        val client = Logger(logBody = true, logHeaders = false)(http)

        val transactor = Transactor.fromDriverManager[IO](
          driver = "org.postgresql.Driver",
          url = "jdbc:postgresql://localhost:5432/sclaus",
          user = "postgres",
          password = "postgres",
          logHandler = None,
        )
        val repository = PostgresRepository.make
        val storage    = UserStorage.make(repository, funcK(transactor))

        given api: Api[IO] = BotApi(http = client, baseUrl = s"https://api.telegram.org/bot$token")
        val bot            = TelegramBot(storage, calendar)
        bot.start()
      }
    } yield ExitCode.Success

}
