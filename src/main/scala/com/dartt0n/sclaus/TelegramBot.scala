package com.dartt0n.sclaus

import cats.effect.Async
import cats.Parallel
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import com.dartt0n.sclaus.service.UserStorage
import org.joda.time.DateTime

class TelegramBot[F[_]](
  storage: UserStorage[F],
  calendar: EventCalendar,
)(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] =
    for {
      _ <- msg.text.filter(_.toLowerCase().startsWith("/start")).fold(asyncF.unit)(_ => onStartCommand(msg))

    } yield ()

  private def onStartCommand(msg: Message): F[Unit] =
    for {
      // if message is from not a private chat then exit
      _ <- asyncF.raiseWhen(msg.chat.`type` != "private")(
        throw RuntimeException("this bot can only be used in private chats"),
      )

      // if message authors is not specified then exit
      telegramUser <- msg.from.fold {
        asyncF.raiseError(throw RuntimeException("unknown author of the message"))
      } { user =>
        asyncF.pure(user)
      }

      // if message author is a bot then exit
      _ <- asyncF.raiseWhen(telegramUser.isBot)(
        throw RuntimeException("bot is not allowed to use this bot"),
      )

      // obtain localization for user
      language = telegramUser.languageCode.flatMap(languages.fromIETFTag).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      // try read existing user
      maybeAlreadyRegisteredUser <- storage.read(UserID(telegramUser.id))

      // if user not found then create new one
      maybeUser <- maybeAlreadyRegisteredUser.fold(
        err =>
          storage.create(
            CreateUser(
              id = UserID(telegramUser.id),
              firstName = Some(telegramUser.firstName),
              lastName = telegramUser.lastName,
              username = telegramUser.username,
              language = language,
              preferences = List.empty,
              state =
                if DateTime.now().isBefore(DateTime(calendar.stage1.end))
                then READY
                else LATECOMER,
            ),
          ),
        user => asyncF.pure(Right(user)),
      )

      // if user creation failed then exit
      user <- maybeUser.fold(
        err => asyncF.raiseError(throw RuntimeException("unknown user")),
        user => asyncF.pure(user),
      )

      _ <- sendMessage(ChatIntId(msg.chat.id), dialog.greeting(user)).exec.void
    } yield ()

}
