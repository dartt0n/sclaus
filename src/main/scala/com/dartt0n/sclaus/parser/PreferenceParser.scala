package com.dartt0n.sclaus.parser

import com.dartt0n.sclaus.domain.errors.AppError

trait PreferenceParser[F[_]] {
  def parse(rawText: String): F[Either[PreferenceParserError, List[String]]]
}

sealed trait PreferenceParserError extends AppError

object PreferenceParserError {
  case object InvalidFormat
}
