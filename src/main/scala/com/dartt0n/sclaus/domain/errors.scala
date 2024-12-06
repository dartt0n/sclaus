package com.dartt0n.sclaus.domain

object errors {

  enum AppError(val message: String, val cause: Option[Throwable] = None) {

    case AlreadyExist(override val cause: Option[Throwable] = None)
        extends AppError("user with the same telegram id already exists", cause)

    case NotFound(override val cause: Option[Throwable] = None)
        extends AppError("user with the specified telegram id not found", cause)

  }

}
