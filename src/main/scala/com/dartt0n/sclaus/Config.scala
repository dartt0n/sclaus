package com.dartt0n.sclaus

import io.circe._
import io.circe.generic.auto._

final case class Config(
  telegram: TelegramConfig,
  database: DatabaseConfig,
  event: EventConfig,
)

final case class TelegramConfig(
  token: String,
)

final case class DatabaseConfig(
  driver: String,
  url: String,
  username: String,
  password: String,
)

sealed trait Stage

object stages {
  case object Registration extends Stage
  case object Preparation  extends Stage
  case object Final        extends Stage
}

given Encoder[Stage] with

  def apply(stage: Stage): Json = Json.fromString(stage match
    case stages.Registration => "REGISTRATION"
    case stages.Preparation  => "PREPARATION"
    case stages.Final        => "FINAL",
  )

given Decoder[Stage] = {
  Decoder.decodeString.emap { s =>
    s match
      case "REGISTRATION" => Right(stages.Registration)
      case "PREPARATION"  => Right(stages.Preparation)
      case "FINAL"        => Right(stages.Final)
      case _              => Left(s"$s is not a valid stage")
  }
}

case class EventConfig(
  stage: Stage,
)
