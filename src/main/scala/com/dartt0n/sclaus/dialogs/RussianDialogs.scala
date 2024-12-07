package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain._

object RussianDialogs extends Dialogs {
  def greeting(user: User): String = s"Привет, ${user.firstName.getOrElse("пользователь")}!"
}
