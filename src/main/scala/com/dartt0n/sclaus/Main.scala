package com.dartt0n.sclaus

import cats._
import cats.effect._
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits._
import com.dartt0n.sclaus.repository.PostgresRepository
import com.dartt0n.sclaus.service.UserStorage
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import io.circe.config.parser
import io.circe.generic.auto._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi

object Main extends IOApp {

  private def funcK(transactor: Transactor[IO]): ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    def apply[A](fa: ConnectionIO[A]): IO[A] = transactor.trans.apply(fa)
  }

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- parser.decodeF[IO, Config]()

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

        given api: Api[IO] = BotApi(http = client, baseUrl = s"https://api.telegram.org/bot${config.telegram.token}")
        val bot            = TelegramBot(storage, config.calendar)

        bot.start()
      }
    } yield ExitCode.Success

}
