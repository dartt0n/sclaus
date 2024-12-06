package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high.{Api, LongPollBot}
import telegramium.bots.high.implicits._
import com.dartt0n.sclaus.dialogs._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.Language

class TelegramBot[F[_]](using api: Api[F], asyncF: Async[F], parallel: Parallel[F]) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] =

    val language: Language = msg.from.flatMap(_.languageCode) match
      case Some("ru") => RUS
      case _          => ENG

    val dialogs = language match
      case RUS => RussianDialogs
      case ENG => EnglishDialogs

    sendMessage(chatId = ChatIntId(msg.chat.id), text = dialogs.greeting()).exec.void

}
