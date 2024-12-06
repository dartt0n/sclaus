package com.dartt0n.sclaus.repository.user

import com.dartt0n.sclaus.domain.Language
import com.dartt0n.sclaus.domain.languages._
import doobie.util.meta.Meta
import doobie.postgres.implicits._
import com.dartt0n.sclaus.domain.CreateUser
import doobie.util.query.Query0
import com.dartt0n.sclaus.domain.User
import org.joda.time.DateTime
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import com.dartt0n.sclaus.domain.UserID
import com.dartt0n.sclaus.domain.UpdateUser

object PostgresUserRepository {

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

  given Meta[UserID] =
    Meta[Long].imap(UserID.apply)(_.toLong())

  given Meta[DateTime] =
    Meta[java.sql.Timestamp].imap(ts => DateTime(ts.getTime))(dt => new java.sql.Timestamp(dt.getMillis()))

  object queries {

    def createQuery(user: CreateUser): Query0[User] =
      sql"""
        INSERT INTO users (id, createdAt, updatedAt, deletedAt, firstName, lastName, username, language, preferences)
        VALUES (
            ${user.id}, ${DateTime.now()}, ${DateTime.now()}, ${None}, ${user.firstName},
            ${user.lastName}, ${user.username}, ${user.language}, ${user.preferences}
        ) RETURNING *;
      """.query[User]

    def readQuery(id: UserID): Query0[User] =
      sql"""
        SELECT * FROM users
        WHERE id=${id} AND deletedAt IS NULL;
      """.query[User]

    def deleteQuery(id: UserID): Query0[User] =
      sql"""
        UPDATE users
        SET deletedAt=${DateTime.now()}
        WHERE id=${id} AND deletedAt IS NULL
        RETURNING *;
      """.query[User]

    def updateQuery(user: UpdateUser): Query0[User] =
      (
        fr"""
            UPDATE users
            SET updateTime=${DateTime.now}
          """
          ++ user.firstName.fold(fr"")(update => fr"SET firstName=${update}")
          ++ user.lastName.fold(fr"")(update => fr"SET lastName=${update}")
          ++ user.username.fold(fr"")(update => fr"SET username=${update}")
          ++ user.language.fold(fr"")(update => fr"SET language=${update}")
          ++ user.preferences.fold(fr"")(update => fr"SET preferences=${update}")
          ++ fr"""
            WHERE id=${user.id} AND deletedAt IS NULL
            RETURNING *;
          """
      ).query[User]

  }

}
