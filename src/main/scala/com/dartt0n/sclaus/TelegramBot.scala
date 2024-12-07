package com.dartt0n.sclaus

import cats.effect.Async
import cats.Parallel
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import com.dartt0n.sclaus.service.UserStorage

class TelegramBot[F[_]](
  storage: UserStorage[F],
)(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] = {
    for {
      telegramUser <- msg.from.fold {
        asyncF.raiseError(throw RuntimeException("unknown author of the message"))
      } { user =>
        asyncF.pure(user)
      }

      language = telegramUser.languageCode.flatMap(languages.fromIETFTag).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      maybeUser <- storage.read(UserID(telegramUser.id))
      user = maybeUser.fold(
        err => asyncF.raiseError(throw RuntimeException("unknown user")),
        user => asyncF.pure(user),
      )

      _ <- sendMessage(ChatIntId(msg.chat.id), dialog.greeting()).exec.void
    } yield ()

  }

}
