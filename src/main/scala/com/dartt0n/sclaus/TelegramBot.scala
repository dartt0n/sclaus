package com.dartt0n.sclaus

import cats.Parallel
import cats.effect.Async
import cats.syntax.all._
import com.dartt0n.sclaus.dialogs.Dialogs
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import com.dartt0n.sclaus.service._
import telegramium.bots.ChatIntId
import telegramium.bots.Html
import telegramium.bots.KeyboardButton
import telegramium.bots.Message
import telegramium.bots.ReplyKeyboardMarkup
import telegramium.bots.ReplyKeyboardRemove
import telegramium.bots.ReplyParameters
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.foption._
import tofu.syntax.logging._

class TelegramBot[F[_]: Logging.Make](
  repo: UserRepositoryService[F],
  parser: ParserService[F],
  currentStage: Stage,
)(using
  api: Api[F],          // required by LongPollBot
  asyncF: Async[F],     // required by LongPollBot
  parallel: Parallel[F],// required by LongPollBot
) extends LongPollBot[F](api) {

  private given logging: Logging[F] = Logging.Make[F].forService[TelegramBot[F]]

  def startupHook(): F[Unit] = (for {
    _ <- info"startup hook triggered"
    _ <- asyncF.whenA(currentStage == stages.Preparation) {
      debug"stage switched to preparation, revealing targets"
        >> repo.listAll().flatMap {
          case Left(error) =>
            asyncF.raiseError(Exception("failed to retrieve users from database"))
          case Right(users) =>
            users.parTraverseFilter { u =>
              val dialog = Dialogs.fromLanguage(u.language)
              u.target.pure.flatMapF { id =>
                repo.read(id).flatMap {
                  case Left(value)  => None.pure
                  case Right(value) => sendMessage(ChatIntId(u.id.toLong), dialog.revealTarget(value)).exec.as(Some(()))
                }
              }
            }
        }
    }
  } yield ()).handleErrorWith(err => info"startup hook failed: ${err.getMessage()}")

  def shutdownHook(): F[Unit] = (for {
    _ <- info"shutdown hook triggered"
  } yield ()).handleErrorWith(err => info"shutdown hook failed: ${err.getMessage()}")

  override def onMessage(msg: Message): F[Unit] = {
    (for {
      _ <- debug"received message in chat ${msg.chat.id}"

      telegramUser <- // Try obtain message author, skip if it is unknown
        msg.from.fold(asyncF.raiseError(Exception("message author is not specified")))(_.pure)

      _ <- // Skip if the author is a bot
        asyncF.raiseWhen(telegramUser.isBot)(Exception("bot is not allowed to use this bot"))

      _ <- // Extract language and build a dialog system
        debug"user language is ${telegramUser.languageCode.getOrElse("unknown")}"
      language = telegramUser.languageCode.flatMap(languages.fromIETF).getOrElse(ENG)
      dialog   = Dialogs.fromLanguage(language)

      _ <- // Skip if message is not in private chat, while sending warning message
        asyncF.whenA(msg.chat.`type` != "private") {
          onGroupMessage(msg, dialog)
        }

      _ <- msg.text.getOrElse("").toLowerCase().startsWith("/start") match {
        case true => onStartCommand(msg, telegramUser, language, dialog)
        case false =>
          repo
            .read(UserID(telegramUser.id))
            .getOrElseF(asyncF.raiseError(Exception("user not found")))
            .flatMap(user => stateMatch(msg, user, dialog))
      }

    } yield ()).handleErrorWith(err => info"error while processing message: ${err.getMessage()}")
  }

  private def stateMatch(msg: Message, user: User, dialog: Dialogs): F[Unit] = {
    debug"processing ${user.state.getClass.getName}, ${msg.text}"
      >> (user.state match {
        case states.READY if msg.text.getOrElse("") == dialog.greetingReplyButton() =>
          onGreetingReply(msg, user, dialog)

        case states.GREETING_ANSWERED if msg.text.getOrElse("") == dialog.rulesReplyButton() =>
          onRulesReply(msg, user, dialog)

        case states.RULES_ANSWERED if msg.text.getOrElse("") == dialog.timelineReplyButton() =>
          onTimelineReply(msg, user, dialog)

        case states.TIMELINE_ANSWERED =>
          onPreferencesReply(msg, user, dialog)

        case states.PREFERENCES_ANSWERED if msg.text.getOrElse("") == dialog.prefButtonOk() =>
          onPreferencesReplyOk(msg, user, dialog)

        case states.PREFERENCES_ANSWERED if msg.text.getOrElse("") == dialog.prefButtonErr() =>
          onPreferencesReplyErr(msg, user, dialog)

        case _ => asyncF.raiseError(Exception("unprocessed state"))
      })
  }

  private def onGroupMessage(msg: Message, dialog: Dialogs): F[Unit] =
    debug"ignoring message from non-private chat"
      >> sendMessage(
        ChatIntId(msg.chat.id),
        dialog.onlyPrivateChatsAllowed(),
        parseMode = Some(Html),
        replyParameters = Some(ReplyParameters(messageId = msg.messageId)),
      ).exec.void
      >> asyncF.raiseError(Exception("this bot can only be used in private chats"))

  private def onStartCommand(
    msg: Message,
    telegramUser: telegramium.bots.User,
    language: Language,
    dialog: Dialogs,
  ): F[Unit] = for {
    _ <- asyncF.unlessA(currentStage == stages.Registration) {
      debug"registration is closed, denying request with message"
        >> sendMessage(ChatIntId(msg.chat.id), dialog.registrationIsClosed(), parseMode = Some(Html)).exec.void
        >> asyncF.raiseError(Exception("registration is closed"))
    }

    user <- debug"reading user ${telegramUser.id} from storage"
      >> repo.read(UserID(telegramUser.id)).getOrElseF {
        repo
          .create(
            CreateUser(
              id = UserID(telegramUser.id),
              firstName = Some(telegramUser.firstName),
              lastName = telegramUser.lastName,
              username = telegramUser.username,
              language = language,
              preferences = List.empty,
              state = READY,
              target = None,
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
      parseMode = Some(Html),
      replyMarkup = Some(
        ReplyKeyboardMarkup(
          isPersistent = Some(false),
          keyboard = List(List(KeyboardButton(dialog.greetingReplyButton()))),
        ),
      ),
    ).exec.void
  } yield ()

  private def onGreetingReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- repo
      .update(UpdateUser(user.id, state = Some(states.GREETING_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("update failed")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.rules(),
      parseMode = Some(Html),
      replyMarkup = Some(
        ReplyKeyboardMarkup(
          isPersistent = Some(true),
          keyboard = List(List(KeyboardButton(dialog.rulesReplyButton()))),
        ),
      ),
    ).exec.void

  } yield ()

  private def onRulesReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- repo
      .update(UpdateUser(user.id, state = Some(states.RULES_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.timeline(),
      parseMode = Some(Html),
      replyMarkup = Some(
        ReplyKeyboardMarkup(
          isPersistent = Some(true),
          keyboard = List(List(KeyboardButton(dialog.timelineReplyButton()))),
        ),
      ),
    ).exec.void

  } yield ()

  private def onTimelineReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- repo
      .update(UpdateUser(user.id, state = Some(states.TIMELINE_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.askPreferences(),
      replyMarkup = Some(ReplyKeyboardRemove(true)),
      parseMode = Some(Html),
    ).exec.void

  } yield ()

  private def onPreferencesReply(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    prefrences <- msg.text.fold(asyncF.raiseError(Exception("empty text"))) { text =>
      parser
        .parse(text)
        .getOrElseF(
          debug"failed to parse users preferences, asking again"
            >> sendMessage(ChatIntId(msg.chat.id), dialog.failedParsePreferences(), parseMode = Some(Html)).exec.void
            >> asyncF.raiseError(Exception("failed to parse")),
        )
    }

    user <- repo
      .update(UpdateUser(user.id, preferences = Some(prefrences), state = Some(states.PREFERENCES_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("update user failed")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.showPreferences(user),
      parseMode = Some(Html),
      replyMarkup = Some(
        ReplyKeyboardMarkup(
          isPersistent = Some(false),
          keyboard = List(
            List(KeyboardButton(dialog.prefButtonOk())),
            List(KeyboardButton(dialog.prefButtonErr())),
          ),
        ),
      ),
    ).exec.void

  } yield ()

  private def onPreferencesReplyOk(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- repo
      .update(UpdateUser(user.id, state = Some(states.REGISTRATION_COMPLETE)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.registrationComplete(),
      parseMode = Some(Html),
      replyMarkup = Some(ReplyKeyboardRemove(true)),
    ).exec.void
  } yield ()

  private def onPreferencesReplyErr(msg: Message, user: User, dialog: Dialogs): F[Unit] = for {
    user <- repo
      .update(UpdateUser(user.id, state = Some(states.TIMELINE_ANSWERED)))
      .getOrElseF(asyncF.raiseError(Exception("user not found")))

    _ <- sendMessage(
      ChatIntId(msg.chat.id),
      dialog.askPreferences(),
      replyMarkup = Some(ReplyKeyboardRemove(true)),
      parseMode = Some(Html),
    ).exec.void
  } yield ()

}
