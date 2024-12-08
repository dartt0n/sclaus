package com.dartt0n.sclaus.service

import cats.~>
import cats.ApplicativeError
import cats.syntax.all._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.errors._
import com.dartt0n.sclaus.repository.Repository

trait UserStorage[F[_]] {

  def create(user: CreateUser): F[Either[AppError.AlreadyExist, User]]

  def read(id: UserID): F[Either[AppError.NotFound, User]]

  def update(user: UpdateUser): F[Either[AppError.NotFound, User]]

  def delete(id: UserID): F[Either[AppError.NotFound, User]]

}

object UserStorage {

  def make[F[_], DB[_]](
    repo: Repository[DB],
    functionK: DB ~> F,
  )(using
    ae: ApplicativeError[F, Throwable],
  ): UserStorage[F] = new UserStorage[F] {

    override def create(user: CreateUser): F[Either[AppError.AlreadyExist, User]] =
      functionK(repo.create(user)).attempt.map {
        case Left(error)         => AppError.AlreadyExist(Some(error)).asLeft
        case Right(Left(error))  => error.asLeft
        case Right(Right(value)) => value.asRight
      }

    override def read(id: UserID): F[Either[AppError.NotFound, User]] =
      functionK(repo.read(id)).attempt.map {
        case Left(error)         => AppError.NotFound(Some(error)).asLeft
        case Right(Left(error))  => error.asLeft
        case Right(Right(value)) => value.asRight
      }

    override def update(user: UpdateUser): F[Either[AppError.NotFound, User]] =
      functionK(repo.update(user)).attempt.map {
        case Left(error)         => AppError.NotFound(Some(error)).asLeft
        case Right(Left(error))  => error.asLeft
        case Right(Right(value)) => value.asRight
      }

    override def delete(id: UserID): F[Either[AppError.NotFound, User]] =
      functionK(repo.delete(id)).attempt.map {
        case Left(error)         => AppError.NotFound(Some(error)).asLeft
        case Right(Left(error))  => error.asLeft
        case Right(Right(value)) => value.asRight
      }

  }

}
