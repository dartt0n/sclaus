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

  def onlyPrivateChatsAllowed(): String = {
    """👋 Привет!
       |
       |⚠️ Пожалуйста, обратите внимание, я общаюсь только в личных сообщениях
       |
       |🤫 Здесь не стоит делиться своими секретиками
       |""".stripMargin
  }

}
