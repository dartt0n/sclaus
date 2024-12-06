package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.Language

object RussianDialogs extends Translation[Language.RUS.type] {
  def greeting(): String = "Привет!"
}
