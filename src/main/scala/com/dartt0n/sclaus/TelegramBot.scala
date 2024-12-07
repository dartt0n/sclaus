package com.dartt0n.sclaus

import cats.effect.Async
import cats.Parallel
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import telegramium.bots._
import telegramium.bots.high._
import telegramium.bots.high.implicits._

class TelegramBot[F[_]]()(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] = {
    for {
      user <- msg.from.fold {
        asyncF.raiseError(throw RuntimeException("unknown author of the message"))
      } { user =>
        asyncF.pure(user)
      }

      language = user.languageCode.flatMap(languages.fromIETFTag).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      _ <- sendMessage(ChatIntId(msg.chat.id), dialog.greeting()).exec.void
    } yield ()

  }

}
