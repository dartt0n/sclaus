package com.dartt0n.sclaus

import cats._
import cats.effect._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.dartt0n.sclaus.repository.PostgresRepository
import com.dartt0n.sclaus.service.UserStorage
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import telegramium.bots.high.{Api, BotApi}

object Main extends IOApp {

  private def funcK(transactor: Transactor[IO]): ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    def apply[A](fa: ConnectionIO[A]): IO[A] = transactor.trans.apply(fa)
  }

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config
        .load()
        .fold(
          err => IO.raiseError(new RuntimeException(s"failed to load config: $err")),
          cfg => IO.pure(cfg),
        )

      _ <- IO.println("Ho, ho ho! Merry Christmas 🎅!")
      _ <- BlazeClientBuilder[IO].resource.use { http =>
        val client = Logger(logBody = true, logHeaders = false)(http)

        val transactor = Transactor.fromDriverManager[IO](
          driver = config.database.driver,
          url = config.database.url,
          user = config.database.username,
          password = config.database.password,
          logHandler = None,
        )
        val repository = PostgresRepository.make
        val storage    = UserStorage.make(repository, funcK(transactor))

        given api: Api[IO] = BotApi(http = client, baseUrl = s"https://api.telegram.org/bot${config.token}")
        val bot            = TelegramBot(storage, config.calendar)

        bot.start()
      }
    } yield ExitCode.Success

}
