package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.Language
import com.dartt0n.sclaus.domain.languages._

trait Dialogs {
  def greeting(user: User): String
}

object Dialogs {

  def fromLanguage(language: Language): Dialogs = language match {
    case RUS => RussianDialogs
    case ENG => EnglishDialogs
  }

}
