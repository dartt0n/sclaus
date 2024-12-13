package com.dartt0n.sclaus.parser

import cats.effect.IO
import cats.implicits._
import scala.util.matching.Regex

final class StaticPreferenceParser extends PreferenceParser[IO] {
  import StaticPreferenceParser._

  def parse(rawText: String): IO[Either[PreferenceParserError, List[String]]] =
    if bulletPointPattern.matches(rawText)
      // toRight does not work???
    then Right(bulletPointPattern.findAllMatchIn(rawText).map(_.group(0)).toList).pure
    else Right(rawText.split("\n").toList).pure

}

object StaticPreferenceParser {
  // bullet point symbols: https://en.wikipedia.org/wiki/Bullet_(typography)

  val bulletPointPattern: Regex =
    """^[\u2022,\u2023,\u2043,\u204C,\u204D,\u2219,\u25CB,\u25CF,\u25D8,\u25E6,\u2619,\u2765,\u2767,\u29BE,\u29BF,\u25C9,-,+,*,\\d+]\\s?.*\$""".r

  def make: StaticPreferenceParser = new StaticPreferenceParser
}
