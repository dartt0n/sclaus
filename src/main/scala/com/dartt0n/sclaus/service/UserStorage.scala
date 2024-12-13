package com.dartt0n.sclaus.service

import cats.~>
import cats.ApplicativeError
import cats.FlatMap
import cats.syntax.all._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.errors._
import com.dartt0n.sclaus.repository.UserRepository
import com.dartt0n.sclaus.repository.UserRepositoryError
import tofu.logging.Logging
import tofu.syntax.logging._

trait UserStorage[F[_]] {

  def create(user: CreateUser): F[Either[UserStorageError, User]]

  def read(id: UserID): F[Either[UserStorageError, User]]

  def update(user: UpdateUser): F[Either[UserStorageError, User]]

  def delete(id: UserID): F[Either[UserStorageError, User]]

}

trait UserStorageError extends AppError

object UserStorageError {
  case class RepositoryError(error: UserRepositoryError) extends UserStorageError
  case class UnexpectedError(error: Throwable)           extends UserStorageError
}

object UserStorage {

  def make[F[_]: FlatMap: Logging.Make, DB[_]](
    repo: UserRepository[DB],
    functionK: DB ~> F,
  )(using
    ApplicativeError[F, Throwable],
  ): UserStorage[F] = new UserStorage[F] {

    private given logging: Logging[F] = Logging.Make[F].forService[UserStorage[F]]

    override def create(user: CreateUser): F[Either[UserStorageError, User]] = {
      debug"creating new user with id ${user.id.toLong}"
        >> functionK(repo.create(user)).attempt.flatMap {
          case Left(error) =>
            errorCause"create user failed with unexpected error" (error)
              >> UserStorageError.UnexpectedError(error).asLeft.pure

          case Right(Left(error)) =>
            error"create user failed with expected error"
              >> UserStorageError.RepositoryError(error).asLeft.pure

          case Right(Right(value)) =>
            debug"create user succesfully finished"
              >> value.asRight.pure
        }

    }

    override def read(id: UserID): F[Either[UserStorageError, User]] = {
      debug"reading new user with id ${id.toLong}"
        >> functionK(repo.read(id)).attempt.flatMap {
          case Left(error) =>
            errorCause"read user failed with unexpected error" (error)
              >> UserStorageError.UnexpectedError(error).asLeft.pure

          case Right(Left(error)) =>
            error"create user failed with expected error"
              >> UserStorageError.RepositoryError(error).asLeft.pure

          case Right(Right(value)) =>
            debug"read user successfully finished"
              >> value.asRight.pure
        }
    }

    override def update(user: UpdateUser): F[Either[UserStorageError, User]] = {
      debug"updatin user with id ${user.id.toLong}"
        >> functionK(repo.update(user)).attempt.flatMap {
          case Left(error) =>
            errorCause"update user failed with unexpected error" (error)
              >> UserStorageError.UnexpectedError(error).asLeft.pure

          case Right(Left(error)) =>
            error"update user failed with expected error"
              >> UserStorageError.RepositoryError(error).asLeft.pure

          case Right(Right(value)) =>
            debug"update user successfully finished"
              >> value.asRight.pure
        }
    }

    override def delete(id: UserID): F[Either[UserStorageError, User]] = {
      debug"deleting user with id ${id.toLong}"
        >> functionK(repo.delete(id)).attempt.flatMap {
          case Left(error) =>
            errorCause"delete user failed with unexpected error" (error)
              >> UserStorageError.UnexpectedError(error).asLeft.pure

          case Right(Left(error)) =>
            error"delete user failed with expected error"
              >> UserStorageError.RepositoryError(error).asLeft.pure

          case Right(Right(value)) =>
            debug"delete user successfully finished"
              >> value.asRight.pure
        }
    }

  }

}
