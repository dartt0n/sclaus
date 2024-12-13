package com.dartt0n.sclaus.service

import cats.~>
import cats.ApplicativeError
import cats.FlatMap
import cats.syntax.all._
import com.dartt0n.sclaus.domain.errors.AppError
import com.dartt0n.sclaus.parser._
import tofu.logging._
import tofu.syntax.logging._

trait ParserService[F[_]] {
  def parse(rawText: String): F[Either[ParseServiceError, List[String]]]
}

trait ParseServiceError extends AppError

object ParseServiceError {
  case class ParserError(error: PreferenceParserError) extends ParseServiceError
  case class UnexpectedError(error: Throwable)         extends ParseServiceError
}

object ParserService {

  def make[F[_]: FlatMap: Logging.Make, PR[_]](
    parser: PreferenceParser[PR],
    functionK: PR ~> F,
  )(using
    ApplicativeError[F, Throwable],
  ) = new ParserService[F] {
    private given Logging[F] = Logging.Make[F].forService[ParserService[F]]

    def parse(rawText: String): F[Either[ParseServiceError, List[String]]] =
      debug"parsing raw text: $rawText"
        >> functionK(parser.parse(rawText)).attempt.flatMap {

          case Left(error) =>
            errorCause"parsing text failed with unexpected error" (error)
              >> ParseServiceError.UnexpectedError(error).asLeft.pure

          case Right(Left(error)) =>
            error"parsing text faield with expected error ${error.getClass.getName}"
              >> ParseServiceError.ParserError(error).asLeft.pure

          case Right(Right(value)) =>
            debug"parsing text successfully finished"
              >> value.asRight.pure
        }

  }

}
