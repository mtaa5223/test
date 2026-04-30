package com.example.infra.Auth

import com.example.domain.Auth.JtiAlreadyConsumedException
import com.example.domain.Auth.Interface.IConsumedJtiRepository
import org.jooq.Configuration
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import java.sql.SQLException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PostgresConsumedJtiRepository : IConsumedJtiRepository {
    override fun consume(cfg: Configuration, jti: String, sub: String, expiresAt: Instant) {
        try {
            DSL.using(cfg)
                .insertInto(
                    DSL.table("consumed_ugs_jti"),
                    DSL.field("jti", String::class.java),
                    DSL.field("sub", String::class.java),
                    DSL.field("expires_at", OffsetDateTime::class.java),
                )
                .values(jti, sub, OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
                .execute()
        } catch (e: DataAccessException) {
            val sqlState = (e.cause as? SQLException)?.sqlState ?: e.sqlState()
            if (sqlState == "23505") throw JtiAlreadyConsumedException(jti)
            throw e
        }
    }
}
