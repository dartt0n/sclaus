package com.dartt0n.sclaus.repository.user

import com.dartt0n.sclaus.domain.Language
import doobie.util.meta.Meta
import doobie.postgres.implicits._

/** mapping from domain Language enum to postgres-compatible Enum */
sealed trait PsqlLanguageEnum

object PsqlLanguageEnum {

  /** russian language */
  case object RUS extends PsqlLanguageEnum

  /** english language */
  case object ENG extends PsqlLanguageEnum

  /** convert postgers-compatible enum value to scala string
    *
    * @param language
    *   \- postgres enum value
    * @return
    *   \- language code in ISO-639-3 format
    */
  def toEnum(language: PsqlLanguageEnum): String = language match {
    case RUS => "RUS"
    case ENG => "ENG"
  }

  /** convert from scala string to postgres-compatible enum value
    *
    * @param languageCode
    *   \- language code in ISO-639-3 format
    * @return
    *   \- postgres enum value
    */
  def fromEnum(languageCode: String): Option[PsqlLanguageEnum] = languageCode match {
    case "RUS" => Some(RUS)
    case "ENG" => Some(ENG)
    case _     => None
  }

  /** implicit conversion from domain Language to postgres-compatible PsqlLanguageEnum */
  given Conversion[Language, PsqlLanguageEnum] with {

    def apply(x: Language): PsqlLanguageEnum = x match {
      case Language.RUS => RUS
      case Language.ENG => ENG
    }

  }

  /** postgres metadata from doobie to work with enum */
  given Meta[PsqlLanguageEnum] = pgEnumStringOpt("languages", PsqlLanguageEnum.fromEnum, PsqlLanguageEnum.toEnum)

}

object PostgresUserRepository {
  ??? // todo: implement methods
}
