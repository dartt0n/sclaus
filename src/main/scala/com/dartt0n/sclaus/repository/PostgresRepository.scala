package com.dartt0n.sclaus.repository

import cats.syntax.applicative._
import cats.syntax.either._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.errors._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import doobie.util._
import org.joda.time.DateTime

private final class PostgresRepository extends Repository[ConnectionIO] {
  import PostgresRepository.queries

  override def create(user: CreateUser): ConnectionIO[Either[AppError.AlreadyExist, User]] =
    queries.readQuery(user.id).option.flatMap {
      case Some(user) => AppError.AlreadyExist().asLeft.pure
      case None =>
        queries.createQuery(user).option flatMap {
          case Some(user) => user.asRight.pure
          case None       => AppError.AlreadyExist().asLeft.pure
        }
    }

  override def read(id: UserID): ConnectionIO[Either[AppError.NotFound, User]] =
    queries.readQuery(id).option.flatMap {
      case Some(value) => value.asRight.pure
      case None        => AppError.NotFound().asLeft.pure
    }

  override def update(user: UpdateUser): ConnectionIO[Either[AppError.NotFound, User]] =
    queries.updateQuery(user).option.flatMap {
      case Some(value) => value.asRight.pure
      case None        => AppError.NotFound().asLeft.pure
    }

  override def delete(id: UserID): ConnectionIO[Either[AppError.NotFound, User]] =
    queries.deleteQuery(id).option.flatMap {
      case Some(value) => value.asRight.pure
      case None        => AppError.NotFound().asLeft.pure
    }

}

object PostgresRepository {

  def make = new PostgresRepository()

  given Meta[Language] = pgEnumStringOpt(
    "languages",
    {
      case "RUS" => Some(RUS)
      case "ENG" => Some(ENG)
      case _     => None
    },
    {
      case RUS => "RUS"
      case ENG => "ENG"
    },
  )

  given Meta[State] = pgEnumStringOpt(
    "states",
    {
      case "READY"      => Some(READY)
      case "LATECOMER"  => Some(LATECOMER)
      case "REGISTERED" => Some(REGISTERED)
      case _            => None
    },
    {
      case READY      => "READY"
      case LATECOMER  => "LATECOMER"
      case REGISTERED => "REGISTERED"
    },
  )

  given Meta[UserID] =
    Meta[Long].imap(UserID.apply)(_.toLong)

  given Meta[DateTime] =
    Meta[java.sql.Timestamp].imap(ts => DateTime(ts.getTime))(dt => new java.sql.Timestamp(dt.getMillis))

  private object queries {

    def createQuery(user: CreateUser): Query0[User] =
      sql"""
        INSERT INTO users (
         "id", "createdAt", "updatedAt", "deletedAt", "firstName",
         "lastName", "username", "language", "preferences", "state"
        ) VALUES (
            ${user.id}, ${DateTime.now()}, ${DateTime.now()}, ${Option.empty[DateTime]}, ${user.firstName},
            ${user.lastName}, ${user.username}, ${user.language}, ${user.preferences}, ${user.state}
        ) RETURNING *;
      """.query[User]

    def readQuery(id: UserID): Query0[User] =
      sql"""
        SELECT * FROM users
        WHERE "id"=$id AND "deletedAt" IS NULL;
      """.query[User]

    def deleteQuery(id: UserID): Query0[User] =
      sql"""
        UPDATE users
        SET "deletedAt"=${DateTime.now()}
        WHERE "id"=$id AND "deletedAt" IS NULL
        RETURNING *;
      """.query[User]

    def updateQuery(user: UpdateUser): Query0[User] =
      (
        fr"""
            UPDATE users
            SET "updateTime"=${DateTime.now}
          """
          ++ user.firstName.fold(fr"")(update => fr"""SET "firstName"=$update""")
          ++ user.lastName.fold(fr"")(update => fr"""SET "lastName"=$update""")
          ++ user.username.fold(fr"")(update => fr"""SET "username"=$update""")
          ++ user.language.fold(fr"")(update => fr"""SET "language"=$update""")
          ++ user.preferences.fold(fr"")(update => fr"""SET "preferences"=$update""")
          ++ user.state.fold(fr"")(update => fr"""SET "state"=$update""")
          ++ fr"""
            WHERE "id"=${user.id} AND "deletedAt" IS NULL
            RETURNING *;
          """
      ).query[User]

  }

}
