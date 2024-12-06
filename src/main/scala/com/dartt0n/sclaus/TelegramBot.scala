package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high.{Api, LongPollBot}
import telegramium.bots.high.implicits._
import com.dartt0n.sclaus.dialogs.RussianDialogs
import com.dartt0n.sclaus.dialogs.EnglishDialogs

class TelegramBot[F[_]](using api: Api[F], asyncF: Async[F], parallel: Parallel[F]) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] =
    // user id : msg.from.map(_.id)
    // todo: 1. fetch from db
    // todo: 2. retrieve user state
    // todo: 3. handle according to state

    // it would be better to use implicits here
    val message = msg.from.flatMap(_.languageCode) match
      case Some("ru") => RussianDialogs.greeting()
      case _          => EnglishDialogs.greeting()

    sendMessage(chatId = ChatIntId(msg.chat.id), text = message).exec.void

}
