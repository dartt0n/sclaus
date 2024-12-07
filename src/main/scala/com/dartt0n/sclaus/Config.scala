package com.dartt0n.sclaus

import java.time.LocalDateTime
import cats.syntax.either._
import io.circe._
import io.circe.generic.auto._
import java.io.FileReader
import scala.util.Try

final case class Config(
  token: String,
  database: DatabaseConfig,
  calendar: EventCalendar,
)

final case class DatabaseConfig(
  driver: String,
  url: String,
  username: String,
  password: String,
)

final case class StageDateRange(
  begin: LocalDateTime,
  end: LocalDateTime,
)

final case class EventCalendar(
  stage1: StageDateRange,
  stage2: StageDateRange,
  stage3: StageDateRange,
)

object Config {

  def load(path: String = "src/main/scala/resources/app.yml"): Either[Throwable, Config] = {
    Try(new FileReader(path)).toEither
      .map(fileReader => processJson(yaml.parser.parse(fileReader)))
      .flatten
  }

  private def processJson(json: Either[ParsingFailure, Json]): Either[Error, Config] =
    json
      .leftMap(err => err: Error)
      .flatMap(_.as[Config])

}
