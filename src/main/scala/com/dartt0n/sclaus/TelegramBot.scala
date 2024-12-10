package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import com.dartt0n.sclaus.service.UserStorage
import telegramium.bots.ChatIntId
import telegramium.bots.KeyboardButton
import telegramium.bots.Message
import telegramium.bots.ReplyKeyboardMarkup
import telegramium.bots.ReplyParameters
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import tofu.logging.Logging
import tofu.syntax.logging._

class TelegramBot[F[_]: Logging.Make](
  storage: UserStorage[F],
  currentStage: Stage,
)(using
  api: Api[F],
  asyncF: Async[F],
  parallel: Parallel[F],
) extends LongPollBot[F](api) {

  private given logging: Logging[F] = Logging.Make[F].forService[TelegramBot[F]]

  override def onMessage(msg: Message): F[Unit] = {
    (for {
      _ <- debug"received message in chat ${msg.chat.id}"

      // Try obtain message author, skip if it is unknown
      telegramUser <- msg.from.fold {
        debug"message author is not specified"
          *> asyncF.raiseError(throw Exception("message author is not specified"))
      } { user =>
        debug"message from user ${user.id}"
          *> asyncF.pure(user)
      }

      // Skip if the author is a bot
      _ <- asyncF.whenA(telegramUser.isBot)(
        debug"ignoring message from bot"
          *> asyncF.raiseError(throw Exception("bot is not allowed to use this bot")),
      )

      // Extract language and building dialog system
      _ <- debug"user language is ${telegramUser.languageCode.getOrElse("unknown")}"
      language = telegramUser.languageCode.flatMap(languages.fromIETF).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      // Skip if message is not in private chat, while sending warning message
      _ <-
        asyncF.whenA(msg.chat.`type` != "private")(
          debug"ignoring message from non-private chat"
            *> sendMessage(
              ChatIntId(msg.chat.id),
              dialog.onlyPrivateChatsAllowed(),
              replyParameters = Some(ReplyParameters(messageId = msg.messageId)),
            ).exec.void.flatMap(_ => asyncF.raiseError(throw Exception("this bot can only be used in private chats"))),
        )

      // Handle /start command
      _ <- msg.text
        .filter(_.toLowerCase().startsWith("/start"))
        .fold(asyncF.unit) { _ =>
          asyncF.unit
            <* debug"received /start command"
            *> onStartCommand(msg, telegramUser, language, dialog)
            <* debug"successfully processed /start command"
        }
        .handleErrorWith(err => error"error while processing /start command: ${err.getMessage()}")

      // Handle greeting reply
      _ <- msg.text
        .filter(_ == dialog.greetingReplyButton())
        .fold(asyncF.unit) { _ =>
          asyncF.unit
            <* debug"received answer to greeing message"
            *> onGreetingReply(msg, telegramUser, dialog)
            <* debug"successfully processed answer to greeting message"
        }
        .handleErrorWith(err => error"error while processing reply to greeting message: ${err.getMessage()}")

      // Handle rules reply
      _ <- msg.text
        .filter(_ == dialog.rulesReplyButton())
        .fold(asyncF.unit) { _ =>
          asyncF.unit
            <* debug"received answer to rules message"
            *> onRulesReply(msg, telegramUser, dialog)
            <* debug"successfully processed answer to rules message"
        }
        .handleErrorWith(err => error"error while processing reply to rules message: ${err.getMessage()}")

    } yield ()).handleErrorWith(err => info"error while processing message: ${err.getMessage()}")
  }

  private def onStartCommand(
    msg: Message,
    telegramUser: telegramium.bots.User,
    language: Language,
    dialog: Dialogs,
  ): F[Unit] = {
    for {
      maybeAlreadyRegisteredUser <- debug"reading user ${telegramUser.id} from storage"
        *> storage
          .read(UserID(telegramUser.id))
          .flatTap {
            case Left(err)    => debug"user ${telegramUser.id} not found in storage"
            case Right(value) => debug"user ${telegramUser.id} found in storage"
          }

      isLateComer = currentStage != stages.Registration

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

      _ <- sendMessage(
        ChatIntId(msg.chat.id),
        dialog.greeting(user),
        replyMarkup = Some(
          ReplyKeyboardMarkup(
            isPersistent = Some(true),
            keyboard = List(List(KeyboardButton(dialog.greetingReplyButton()))),
          ),
        ),
      ).exec.void
    } yield ()
  }

  private def onGreetingReply(
    msg: Message,
    telegramUser: telegramium.bots.User,
    dialog: Dialogs,
  ): F[Unit] = {
    for {
      user <- debug"reading user ${telegramUser.id} from storage"
        *> storage.read(UserID(telegramUser.id)).flatMap {
          case Left(err)   => asyncF.raiseError(throw RuntimeException("user not found"))
          case Right(user) => asyncF.pure(user)
        }

      _ <- asyncF.raiseUnless(user.state == states.READY)(throw RuntimeException("invalid state, skipping"))

      user <-
        debug"updating user ${telegramUser.id} state"
          *> storage.update(UpdateUser(user.id, state = Some(states.GREETING_ANSWERED))).flatMap {
            case Left(err)   => asyncF.raiseError(throw RuntimeException("user not found"))
            case Right(user) => asyncF.pure(user)
          }

      _ <- sendMessage(
        ChatIntId(msg.chat.id),
        dialog.rules(),
        replyMarkup = Some(
          ReplyKeyboardMarkup(
            isPersistent = Some(true),
            keyboard = List(List(KeyboardButton(dialog.rulesReplyButton()))),
          ),
        ),
      ).exec.void

    } yield ()
  }

  private def onRulesReply(
    msg: Message,
    telegramUser: telegramium.bots.User,
    dialog: Dialogs,
  ): F[Unit] = {
    for {
      user <- debug"reading user ${telegramUser.id} from storage"
        *> storage.read(UserID(telegramUser.id)).flatMap {
          case Left(err)   => asyncF.raiseError(throw RuntimeException("user not found"))
          case Right(user) => asyncF.pure(user)
        }

      _ <- asyncF.raiseUnless(user.state == states.GREETING_ANSWERED)(throw RuntimeException("invalid state, skipping"))

      user <- debug"updating user ${telegramUser.id} state"
        *> storage.update(UpdateUser(user.id, state = Some(states.RULES_ANSWERED))).flatMap {
          case Left(err)   => asyncF.raiseError(throw RuntimeException("user not found"))
          case Right(user) => asyncF.pure(user)
        }

      _ <- sendMessage(
        ChatIntId(msg.chat.id),
        dialog.timeline(),
        replyMarkup = Some(
          ReplyKeyboardMarkup(
            isPersistent = Some(true),
            keyboard = List(List(KeyboardButton(dialog.timelineReplyButton()))),
          ),
        ),
      ).exec.void

    } yield ()
  }

}
