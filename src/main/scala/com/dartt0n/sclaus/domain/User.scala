package com.dartt0n.sclaus.domain

import org.joda.time.DateTime

object UserID {
  opaque type T = Long

  def apply(value: Long): T = value

  extension (id: T)
    def toLong: Long = id
    def toString     = id.toLong.toString

}

type UserID = UserID.T

sealed trait Language

object languages {
  case object RUS extends Language
  case object ENG extends Language

  def fromIETF(tag: String): Option[Language] = tag match {
    case "ru" => Some(RUS)
    case "en" => Some(ENG)
    case _    => None
  }

  def toIETF(lang: Language): String = lang match {
    case RUS => "ru"
    case ENG => "en"
  }

}

sealed trait State

object states {
  case object READY                extends State
  case object LATECOMER            extends State
  case object GREETING_ANSWERED    extends State
  case object RULES_ANSWERED       extends State
  case object TIMELINE_ANSWERED    extends State
  case object PREFERENCES_ANSWERED extends State
  case object PREFERENCES_EDITING  extends State
  case object TARGET_RECEIVED      extends State
  case object GIFT_CONFIRMED       extends State
}

final case class User(
  id: UserID,
  //
  createdAt: DateTime,
  updatedAt: DateTime,
  deletedAt: Option[DateTime],
  //
  firstName: Option[String],
  lastName: Option[String],
  username: Option[String],
  language: Language,
  //
  preferences: List[String],
  state: State,
)

final case class CreateUser(
  id: UserID,
  //
  firstName: Option[String],
  lastName: Option[String],
  username: Option[String],
  language: Language,
  //
  preferences: List[String],
  state: State,
)

final case class UpdateUser(
  id: UserID,
  //
  firstName: Option[Option[String]] = None,
  lastName: Option[Option[String]] = None,
  username: Option[Option[String]] = None,
  language: Option[Language] = None,
  //
  preferences: Option[List[String]] = None,
  state: Option[State] = None,
)
