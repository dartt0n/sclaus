package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.User

object EnglishDialogs extends Dialogs {
  def greeting(user: User): String = s"Hello, ${user.firstName.getOrElse("user")}!"

  def onlyPrivateChatsAllowed(): String =
    """👋 Hi! 
       |
       |⚠️ Please note, I only communicate in private messages
       |
       |It's not worth sharing your secrets here 🤫
       |""".stripMargin

}
