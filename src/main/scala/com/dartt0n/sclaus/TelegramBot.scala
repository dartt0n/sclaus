package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high.{Api, LongPollBot}
import telegramium.bots.high.implicits._

class TelegramBot[F[_]](using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api):

  override def onMessage(msg: Message): F[Unit] =
    sendMessage(
      chatId = ChatIntId(msg.chat.id),
      text = "Hello!",
    ).exec.void
