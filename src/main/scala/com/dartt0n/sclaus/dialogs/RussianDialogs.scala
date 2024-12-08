package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain._

object RussianDialogs extends Dialogs {
  def greeting(user: User): String = s"Привет, ${user.firstName.getOrElse("пользователь")}!"

  def onlyPrivateChatsAllowed(): String =
    """👋 Привет! 
       |
       |⚠️ Пожалуйста, обратите внимание, я общаюсь только в личных сообщениях
       |
       |Здесь не стоит делиться своими секретиками 🤫
       |""".stripMargin

}
