package com.dartt0n.sclaus.domain

import org.joda.time.DateTime

class UserID {
  opaque type UserID = Long
  def apply(value: Long): UserID            = value
  extension (id: UserID) def toLong(): Long = id
}

enum Language {
  case RUS
  case ENG
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
)
