package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.Language

trait Translation[L <: Language] {
  def greeting(): String
}
