package com.dartt0n.sclaus

import io.circe._
import io.circe.generic.auto._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.util.Try

final case class Config(
  telegram: TelegramConfig,
  database: DatabaseConfig,
  calendar: EventCalendarConfig,
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

val DTF = DateTimeFormat.forPattern("dd MMM YYYY HH:mm ZZZ")

given Encoder[DateTime] with
  def apply(dt: DateTime): Json = Json.fromString(dt.toString(DTF))

given Decoder[DateTime] =
  Decoder.decodeString.emapTry(s => Try { DTF.parseDateTime(s) })

final case class StageDateRange(
  begin: DateTime,
  end: DateTime,
)

final case class EventCalendarConfig(
  stage1: StageDateRange,
  stage2: StageDateRange,
  stage3: StageDateRange,
)
