package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.User

object EnglishDialogs extends Dialogs {
  def greeting(user: User): String = s"Hello, ${user.firstName.getOrElse("user")}!"
}
