package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.Language

object EnglishDialogs extends Translation[Language.ENG.type] {
  def greeting(): String = "Hello!"
}
