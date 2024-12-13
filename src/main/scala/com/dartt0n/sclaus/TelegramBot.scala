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
import tofu.syntax.feither._
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
          *> asyncF.raiseError(Exception("message author is not specified"))
      } { user =>
        debug"message from user ${user.id}"
          *> user.pure
      }

      // Skip if the author is a bot
      _ <- asyncF.whenA(telegramUser.isBot)(
        debug"ignoring message from bot"
          *> asyncF.raiseError(Exception("bot is not allowed to use this bot")),
      )

      // Extract language and build a dialog system
      _ <- debug"user language is ${telegramUser.languageCode.getOrElse("unknown")}"
      language = telegramUser.languageCode.flatMap(languages.fromIETF).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      // Skip if message is not in private chat, while sending warning message
      _ <- asyncF.whenA(msg.chat.`type` != "private") {
        debug"ignoring message from non-private chat"
          *> sendMessage(
            ChatIntId(msg.chat.id),
            dialog.onlyPrivateChatsAllowed(),
            replyParameters = Some(ReplyParameters(messageId = msg.messageId)),
          ).exec.void
          >> asyncF.raiseError(Exception("this bot can only be used in private chats")),
      }

      // Handle /start command
      _ <- asyncF
        .whenA(msg.text.getOrElse("").toLowerCase().startsWith("/start")) {
          debug"received /start command"
            >> onStartCommand(msg, telegramUser, language, dialog)
            >> debug"successfully processed /start command"
        }
        .handleErrorWith(err => error"error while processing /start command: ${err.getMessage()}")

      user <- storage.read(UserID(telegramUser.id)).getOrElseF(asyncF.raiseError(Exception("user not found")))

      // Handle greetings reply
      _ <- asyncF
        .whenA(msg.text.getOrElse("") == dialog.greetingReplyButton() && user.state == states.READY) {
          debug"received answer to greeing message"
            >> onGreetingReply(msg, user, dialog)
            >> debug"successfully processed answer to greeting message"
        }
        .handleErrorWith(err => error"error while processing reply to greeting message: ${err.getMessage()}")

      // Handle rules reply
      _ <- asyncF
        .whenA(msg.text.getOrElse("") == dialog.rulesReplyButton() && user.state == states.GREETING_ANSWERED) {
          debug"received answer to rules message"
            >> onRulesReply(msg, user, dialog)
            >> debug"successfully processed answer to rules message"
        }
        .handleErrorWith(err => error"error while processing reply to rules message: ${err.getMessage()}")

      // Handle timeline reply
      _ <- asyncF
        .whenA(msg.text.getOrElse("") == dialog.timelineReplyButton() && user.state == states.RULES_ANSWERED) {
          debug"received answer to timeline message"
            >> onTimelineReply(msg, user, dialog)
            >> debug"successfully processed answer to timeline message"
        }
        .handleErrorWith(err => error"error while processing reply to timeline message: ${err.getMessage()}")

      // Handle preferences reply
      _ <- asyncF
        .whenA(user.state == states.TIMELINE_ANSWERED) {
          debug"received answer to preferences message"
            >> onPreferencesReply(msg, user, dialog)
            >> debug"successfully processed answer to preferences message"
        }
        .handleErrorWith(err => error"error while processing reply to timeline message: ${err.getMessage()}")

    } yield ()).handleErrorWith(err => info"error while processing message: ${err.getMessage()}")
  }

  private def onStartCommand(
    msg: Message,
    telegramUser: telegramium.bots.User,
    language: Language,
    dialog: Dialogs,
  ): F[Unit] = for {
    _ <- asyncF.unlessA(currentStage == stages.Registration) {
      debug"registration is closed, denying request with message"
        >> sendMessage(ChatIntId(msg.chat.id), dialog.registrationIsClosed()).exec.void
        >> asyncF.raiseError(Exception("registration is closed"))
    }

    user <- debug"reading user ${telegramUser.id} from storage"
      >> storage.read(UserID(telegramUser.id)).getOrElseF {
        storage
          .create(
            CreateUser(
              id = UserID(telegramUser.id),
              firstName = Some(telegramUser.firstName),
              lastName = telegramUser.lastName,
              username = telegramUser.username,
              language = language,
              preferences = List.empty,
              state = READY,
            ),
          )
          .foldF(
            err => asyncF.raiseError(Exception("user creation failed")),
            user => user.pure,
          )
      }

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

  private def onGreetingReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- storage
      .update(UpdateUser(user.id, state = Some(states.GREETING_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("update failed")))

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

  private def onRulesReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- storage
      .update(UpdateUser(user.id, state = Some(states.RULES_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

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

  private def onTimelineReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- storage
      .update(UpdateUser(user.id, state = Some(states.RULES_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.askPreferences(),
      replyMarkup = None,
    ).exec.void

  } yield ()

  private def onPreferencesReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    _ <- ().pure
  } yield ()

}
