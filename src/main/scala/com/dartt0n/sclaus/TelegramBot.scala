package com.dartt0n.sclaus

import cats.effect.Async
import cats.Parallel
import com.dartt0n.sclaus.dialogs._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import telegramium.bots.Message
import telegramium.bots.high.{Api, LongPollBot}

class TelegramBot[F[_]]()(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  def dialogPerLanguage(language: Language): Dialogs = language match {
    case RUS => RussianDialogs
    case ENG => EnglishDialogs
  }

  override def onMessage(msg: Message): F[Unit] = {
    ???
  }

}
