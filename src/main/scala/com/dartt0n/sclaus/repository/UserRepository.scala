package com.dartt0n.sclaus.repository

import com.dartt0n.sclaus.domain.CreateUser
import com.dartt0n.sclaus.domain.UpdateUser
import com.dartt0n.sclaus.domain.User
import com.dartt0n.sclaus.domain.UserID
import com.dartt0n.sclaus.domain.errors._

trait UserRepository[F[_]] {

  def create(user: CreateUser): F[Either[UserRepositoryError.Create, User]]

  def read(id: UserID): F[Either[UserRepositoryError.Read, User]]

  def update(user: UpdateUser): F[Either[UserRepositoryError.Update, User]]

  def delete(id: UserID): F[Either[UserRepositoryError.Delete, User]]

}

/** Errors related to UserRepository */
sealed trait UserRepositoryError extends AppError

object UserRepositoryError {

  /** Creation operation failed */
  sealed trait Create extends UserRepositoryError

  object Create {

    /** User with the same telegram id already exists */
    case object AlreadyExist extends Create

    /** Failed to create user due to database constraints */
    case object Conflict extends Create
  }

  /** Reading operation failed */
  sealed trait Read extends UserRepositoryError

  object Read {

    /** User with the specified id was not found */
    case object NotFound extends Read
  }

  /** Updateing operation failed */
  sealed trait Update extends UserRepositoryError

  object Update {

    /** User with the specified id was not found */
    case object NotFound extends Update

    /** Failed to update user due to database constraints */
    case object Conflict extends Update
  }

  /** Deletion operation failed */
  sealed trait Delete extends UserRepositoryError

  object Delete {

    /** User with the specified id was not found */
    case object NotFound extends Delete
  }

}
