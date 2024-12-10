package com.dartt0n.sclaus.repository

import cats.syntax.applicative._
import cats.syntax.either._
import com.dartt0n.sclaus.domain._
import com.dartt0n.sclaus.domain.errors._
import com.dartt0n.sclaus.domain.languages._
import com.dartt0n.sclaus.domain.states._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import doobie.util._
import org.joda.time.DateTime

private final class PostgresRepository extends Repository[ConnectionIO] {

  import PostgresRepository._

  override def create(user: CreateUser): ConnectionIO[Either[AppError.AlreadyExist, User]] = {
    readQuery(user.id).option.flatMap {
      case Some(user) => AppError.AlreadyExist().asLeft.pure
      case None =>
        createQuery(user).option flatMap {
          case Some(user) => user.asRight.pure
          case None       => AppError.AlreadyExist().asLeft.pure
        }
    }
  }

  override def read(id: UserID): ConnectionIO[Either[AppError.NotFound, User]] =
    readQuery(id).option.flatMap {
      case Some(value) => value.asRight.pure
      case None        => AppError.NotFound().asLeft.pure
    }

  override def update(user: UpdateUser): ConnectionIO[Either[AppError.NotFound, User]] =
    updateQuery(user).option.flatMap {
      case Some(value) => value.asRight.pure
      case None        => AppError.NotFound().asLeft.pure
    }

  override def delete(id: UserID): ConnectionIO[Either[AppError.NotFound, User]] =
    deleteQuery(id).option.flatMap {
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
      case "READY"                => Some(READY)
      case "LATECOMER"            => Some(LATECOMER)
      case "GREETING_ANSWERED"    => Some(GREETING_ANSWERED)
      case "RULES_ANSWERED"       => Some(RULES_ANSWERED)
      case "TIMELINE_ANSWERED"    => Some(TIMELINE_ANSWERED)
      case "PREFERENCES_ANSWERED" => Some(PREFERENCES_ANSWERED)
      case "PREFERENCES_EDITING"  => Some(PREFERENCES_EDITING)
      case "TARGET_RECEIVED"      => Some(TARGET_RECEIVED)
      case "GIFT_CONFIRMED"       => Some(GIFT_CONFIRMED)
      case _                      => None
    },
    {
      case READY                => "READY"
      case LATECOMER            => "LATECOMER"
      case GREETING_ANSWERED    => "GREETING_ANSWERED"
      case RULES_ANSWERED       => "RULES_ANSWERED"
      case TIMELINE_ANSWERED    => "TIMELINE_ANSWERED"
      case PREFERENCES_ANSWERED => "PREFERENCES_ANSWERED"
      case PREFERENCES_EDITING  => "PREFERENCES_EDITING"
      case TARGET_RECEIVED      => "TARGET_RECEIVED"
      case GIFT_CONFIRMED       => "GIFT_CONFIRMED"
    },
  )

  given Meta[UserID] =
    Meta[Long].imap(UserID.apply)(_.toLong)

  given Meta[DateTime] =
    Meta[java.sql.Timestamp].imap(ts => DateTime(ts.getTime))(dt => new java.sql.Timestamp(dt.getMillis))

  def createQuery(user: CreateUser): Query0[User] = {
    sql"""
        INSERT INTO users (
         "id", "createdAt", "updatedAt", "deletedAt", "firstName",
         "lastName", "username", "language", "preferences", "state"
        ) VALUES (
            ${user.id}, ${DateTime.now()}, ${DateTime.now()}, ${Option.empty[DateTime]}, ${user.firstName},
            ${user.lastName}, ${user.username}, ${user.language}, ${user.preferences}, ${user.state}
        ) RETURNING *;
      """.query[User]
  }

  def readQuery(id: UserID): Query0[User] =
    sql"""
        SELECT * FROM users
        WHERE "id"=$id AND "deletedAt" IS NULL;
      """.query[User]

  def deleteQuery(id: UserID): Query0[User] = {
    sql"""
        UPDATE users
        SET "deletedAt"=${DateTime.now()}
        WHERE "id"=$id AND "deletedAt" IS NULL
        RETURNING *;
      """.query[User]
  }

  def updateQuery(user: UpdateUser): Query0[User] = {
    (
      fr"""
            UPDATE users
            SET "updatedAt"=${DateTime.now}
          """
        ++ user.firstName.fold(fr"")(update => fr""", "firstName"=$update""")
        ++ user.lastName.fold(fr"")(update => fr""", "lastName"=$update""")
        ++ user.username.fold(fr"")(update => fr""", "username"=$update""")
        ++ user.language.fold(fr"")(update => fr""", "language"=$update""")
        ++ user.preferences.fold(fr"")(update => fr""", "preferences"=$update""")
        ++ user.state.fold(fr"")(update => fr""", "state"=$update""")
        ++ fr"""
            WHERE "id"=${user.id} AND "deletedAt" IS NULL
            RETURNING *;
          """
    ).query[User]
  }

}
