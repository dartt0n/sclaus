package com.dartt0n.sclaus

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import telegramium.bots.high.{Api, BotApi}
import cats.implicits._

object Main extends IOApp {

  def run(args: List[String]) =
    val token = "" // todo: load from config

    for {
      _ <- IO.println("Ho, ho ho! Merry Christmas 🎅!")
      _ <- BlazeClientBuilder[IO].resource.use { http =>
        val client         = Logger(logBody = true, logHeaders = false)(http)
        given api: Api[IO] = BotApi(http = client, baseUrl = s"https://api.telegram.org/bot$token")
        val bot            = TelegramBot()

        bot.start()
      }
    } yield ExitCode.Success

}
