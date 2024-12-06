package com.dartt0n.sclaus.repository.user

import com.dartt0n.sclaus.domain.CreateUser
import com.dartt0n.sclaus.domain.errors.AppError
import com.dartt0n.sclaus.domain.User
import com.dartt0n.sclaus.domain.UserID
import com.dartt0n.sclaus.domain.UpdateUser

trait Repository[F[_]] {

  def create(user: CreateUser): F[Either[AppError.AlreadyExist, User]]

  def read(id: UserID): F[Either[AppError.NotFound, User]]

  def update(user: UpdateUser): F[Either[AppError.NotFound, User]]

  def delete(id: UserID): F[Either[AppError.NotFound, User]]

}
