package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.Language
import com.dartt0n.sclaus.domain.languages._

trait Dialogs {
  // dialogs
  def greeting(user: User): String
  def greetingReplyButton(): String

  def rules(): String
  def rulesReplyButton(): String

  def timeline(): String
  def timelineReplyButton(): String

  def askPreferences(): String
  def showPreferences(user: User): String

  // system messages
  def onlyPrivateChatsAllowed(): String
}

object Dialogs {

  def fromLanguage(language: Language): Dialogs = language match {
    case RUS => RussianDialogs
    case ENG => EnglishDialogs
  }

}
