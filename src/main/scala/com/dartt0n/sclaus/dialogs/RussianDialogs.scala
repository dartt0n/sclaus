package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain._

object RussianDialogs extends Dialogs {

  def greeting(user: User): String =
    // todo: translate english
    s""""""

  def greetingReplyButton(): String =
    // todo: translate english
    s""""""

  def rules(): String =
    // todo: translate english
    s""""""

  def rulesReplyButton(): String =
    // todo: translate english
    s""""""

  def timeline(): String =
    // todo: translate english
    s""""""

  def timelineReplyButton(): String =
    // todo: translate english
    s""""""

  def askPreferences(): String =
    // todo: translate english
    s""""""

  def showPreferences(user: User): String =
    // todo: translate english
    s""""""

  def replyPreferencesOK(): String  = ""
  def replyPreferencesErr(): String = ""

  def revealTarget(target: User): String =
    // todo: translate englush
    s"""
        |""".stripMargin

  def remindAboutGift(): String =
    // todo: translate englush
    s"""
        |""".stripMargin

  def inviteToParty(): String =
    // todo: translate englush
    s"""
        |""".stripMargin

  def onlyPrivateChatsAllowed(): String =
    """👋 Привет!
       |
       |⚠️ Пожалуйста, обратите внимание, я общаюсь только в личных сообщениях
       |
       |🤫 Здесь не стоит делиться своими секретиками
       |""".stripMargin

  def registrationIsClosed(): String =
    s"""
       |""".stripMargin

  def failedParsePreferences(): String =
    // todo:
    s"""
       |""".stripMargin

}
