package com.dartt0n.sclaus.service

import cats.~>
import cats.ApplicativeError
import cats.FlatMap
import cats.syntax.all._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.errors._
import com.dartt0n.sclaus.repository.Repository
import tofu.logging.Logging
import tofu.syntax.logging._

trait UserStorage[F[_]] {

  def create(user: CreateUser): F[Either[AppError.AlreadyExist, User]]

  def read(id: UserID): F[Either[AppError.NotFound, User]]

  def update(user: UpdateUser): F[Either[AppError.NotFound, User]]

  def delete(id: UserID): F[Either[AppError.NotFound, User]]

}

object UserStorage {

  def make[F[_]: FlatMap: Logging.Make, DB[_]](
    repo: Repository[DB],
    functionK: DB ~> F,
  )(using
    ae: ApplicativeError[F, Throwable],
  ): UserStorage[F] = new UserStorage[F] {

    private given logging: Logging[F] = Logging.Make[F].forService[UserStorage[F]]

    override def create(user: CreateUser): F[Either[AppError.AlreadyExist, User]] = {
      functionK(repo.create(user)).attempt.flatMap {
        case Left(error) =>
          errorCause"create user failed with unexpected error" (error)
            >> AppError.AlreadyExist(Some(error)).asLeft.pure

        case Right(Left(error)) =>
          (error.cause match {
            case None        => error"create user failed with error ${error.getClass().getName()}"
            case Some(cause) => errorCause"create user failed with error ${error.getClass().getName()}" (cause)
          })
            >> error.asLeft.pure

        case Right(Right(value)) =>
          debug"create user successfully finished"
            >> value.asRight.pure
      }
    }

    override def read(id: UserID): F[Either[AppError.NotFound, User]] = {
      functionK(repo.read(id)).attempt.flatMap {
        case Left(error) =>
          errorCause"read user failed with unexpected error" (error)
            >> AppError.NotFound(Some(error)).asLeft.pure

        case Right(Left(error)) =>
          (error.cause match {
            case None        => error"read user failed with error ${error.getClass().getName()}"
            case Some(cause) => errorCause"read user failed with error ${error.getClass().getName()}" (cause)
          })
            >> error.asLeft.pure

        case Right(Right(value)) =>
          debug"read user successfully finished"
            >> value.asRight.pure
      }
    }

    override def update(user: UpdateUser): F[Either[AppError.NotFound, User]] = {
      functionK(repo.update(user)).attempt.flatMap {
        case Left(error) =>
          errorCause"update user failed with unexpected error" (error)
            >> AppError.NotFound(Some(error)).asLeft.pure

        case Right(Left(error)) =>
          (error.cause match {
            case None        => error"update user failed with error ${error.getClass().getName()}"
            case Some(cause) => errorCause"update user failed with error ${error.getClass().getName()}" (cause)
          })
            >> error.asLeft.pure

        case Right(Right(value)) =>
          debug"update user successfully finished"
            >> value.asRight.pure
      }
    }

    override def delete(id: UserID): F[Either[AppError.NotFound, User]] = {
      functionK(repo.delete(id)).attempt.flatMap {
        case Left(error) =>
          errorCause"delete user failed with unexpected error" (error)
            >> AppError.NotFound(Some(error)).asLeft.pure

        case Right(Left(error)) =>
          (error.cause match {
            case None        => error"delete user failed with error ${error.getClass().getName()}"
            case Some(cause) => errorCause"delete user failed with error ${error.getClass().getName()}" (cause)
          })
            >> error.asLeft.pure

        case Right(Right(value)) =>
          debug"delete user successfully finished"
            >> value.asRight.pure

      }
    }

  }

}
