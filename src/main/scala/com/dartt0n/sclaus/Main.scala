package com.dartt0n.sclaus

import cats.~>
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.dartt0n.sclaus.repository.PostgresUserRepository
import com.dartt0n.sclaus.service.UserStorage
import doobie.free.connection.ConnectionIO
import doobie.util.log.LogEvent
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import io.circe.config.parser
import io.circe.generic.auto.deriveDecoder
import org.http4s.blaze.client.BlazeClientBuilder
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi
import tofu.logging.Logging
import tofu.syntax.logging._

object Main extends IOApp {

  private def funcK(transactor: Transactor[IO]): ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    def apply[A](fa: ConnectionIO[A]): IO[A] = transactor.trans.apply(fa)
  }

  given Logging.Make[IO] = Logging.Make.plain[IO]
  given Logging[IO]      = summon[Logging.Make[IO]].byName("com.dartt0n.sclaus")

  given databaseLogger: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] = debug"doobie query: ${logEvent.sql}"
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- parser.decodeF[IO, Config]()

      _ <- info"Ho, ho ho! Merry Christmas 🎅!"
      _ <- BlazeClientBuilder[IO].resource.use { http =>

        val transactor = Transactor.fromDriverManager[IO](
          driver = config.database.driver,
          url = config.database.url,
          user = config.database.username,
          password = config.database.password,
          logHandler = Some(databaseLogger),
        )
        val repository = PostgresUserRepository.make
        val storage    = UserStorage.make(repository, funcK(transactor))

        given api: Api[IO] = BotApi(http = http, baseUrl = s"https://api.telegram.org/bot${config.telegram.token}")
        val bot            = TelegramBot(storage, config.event.stage)

        bot.start()
      }
    } yield ExitCode.Success
  }

}
