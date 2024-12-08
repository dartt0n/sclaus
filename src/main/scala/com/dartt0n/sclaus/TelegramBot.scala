package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import com.dartt0n.sclaus.service.UserStorage
import org.joda.time.DateTime
import telegramium.bots.ChatIntId
import telegramium.bots.Message
import telegramium.bots.ReplyParameters
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import tofu.logging.Logging
import tofu.syntax.logging._

class TelegramBot[F[_]: Logging.Make](
  storage: UserStorage[F],
  calendar: EventCalendarConfig,
)(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  private given logging: Logging[F] = Logging.Make[F].forService[TelegramBot[F]]

  override def onMessage(msg: Message): F[Unit] =
    (for {
      _ <- debug"received message in chat ${msg.chat.id}"

      telegramUser <- msg.from.fold {
        debug"message author is not specified"
          *> asyncF.raiseError(throw Exception("message author is not specified"))
      } { user =>
        debug"message from user ${user.id}"
          *> asyncF.pure(user)
      }

      _ <- asyncF.whenA(telegramUser.isBot)(
        debug"ignoring message from bot"
          *> asyncF.raiseError(throw Exception("bot is not allowed to use this bot")),
      )

      _ <- debug"user language is ${telegramUser.languageCode.getOrElse("unknown")}"
      language = telegramUser.languageCode.flatMap(languages.fromIETFTag).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      _ <- asyncF.whenA(msg.chat.`type` != "private")(
        debug"ignoring message from non-private chat"
          *> sendMessage(
            ChatIntId(msg.chat.id),
            dialog.onlyPrivateChatsAllowed(),
            replyParameters = Some(ReplyParameters(messageId = msg.messageId)),
          ).exec.void.flatMap(_ => asyncF.raiseError(throw Exception("this bot can only be used in private chats"))),
      )

      _ <- msg.text
        .filter(_.toLowerCase().startsWith("/start"))
        .fold { asyncF.unit } { _ =>
          asyncF.unit
            <* debug"received /start command"
            *> onStartCommand(msg, telegramUser, language, dialog)
            <* debug"successfully processed /start command"
        }
        .handleErrorWith(err => error"error while processing /start command: ${err.getMessage()}")

    } yield ()).handleErrorWith(err => info"error while processing message: ${err.getMessage()}")

  private def onStartCommand(
    msg: Message,
    telegramUser: telegramium.bots.User,
    language: Language,
    dialog: Dialogs,
  ): F[Unit] =
    for {
      maybeAlreadyRegisteredUser <- debug"reading user ${telegramUser.id} from storage"
        *> storage
          .read(UserID(telegramUser.id))
          .flatTap {
            case Left(err)    => debug"user ${telegramUser.id} not found in storage"
            case Right(value) => debug"user ${telegramUser.id} found in storage"
          }

      isLateComer = DateTime.now().isAfter(DateTime(calendar.stage1.end))

      // if user not found then create new one
      maybeUser <- maybeAlreadyRegisteredUser.fold(
        err =>
          debug"creating new user with id ${telegramUser.id}"
            *> storage
              .create(
                CreateUser(
                  id = UserID(telegramUser.id),
                  firstName = Some(telegramUser.firstName),
                  lastName = telegramUser.lastName,
                  username = telegramUser.username,
                  language = language,
                  preferences = List.empty,
                  state = if isLateComer then LATECOMER else READY,
                ),
              )
              .flatTap {
                case Right(_) => asyncF.unit
                case Left(err) =>
                  err.cause match {
                    case None        => error"failed to create user with unknown error"
                    case Some(cause) => errorCause"failed to create user with the following error" (cause)
                  }
              },
        user => asyncF.pure(Right(user)),
      )

      user <- maybeUser.fold(
        err => asyncF.raiseError(throw Exception("unknown user")),
        user => asyncF.pure(user),
      )

      _ <- sendMessage(ChatIntId(msg.chat.id), dialog.greeting(user)).exec.void
    } yield ()

}
