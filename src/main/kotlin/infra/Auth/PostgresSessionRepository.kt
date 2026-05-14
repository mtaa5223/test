package com.example.infra.Auth

import com.example.domain.Auth.NewSession
import com.example.domain.Auth.Session
import com.example.domain.Auth.Interface.ISessionRepository
import org.jooq.Configuration
import org.jooq.impl.DSL
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class PostgresSessionRepository : ISessionRepository {

    override fun revokeActiveForUser(
        cfg: Configuration,
        userId: UUID,
        currentDeviceId: String,
    ): Boolean {
        val deviceIdField = DSL.field("device_id", String::class.java)
        val reasonExpr = DSL.case_()
            .`when`(deviceIdField.eq(currentDeviceId), DSL.value("reissued"))
            .otherwise(DSL.value("replaced"))

        val revokedDeviceIds = DSL.using(cfg)
            .update(DSL.table("sessions"))
            .set(DSL.field("revoked_at", OffsetDateTime::class.java), DSL.currentOffsetDateTime())
            .set(DSL.field("revoked_reason", String::class.java), reasonExpr)
            .where(
                DSL.field("user_id", UUID::class.java).eq(userId)
                    .and(DSL.field("revoked_at").isNull),
            )
            .returning(deviceIdField)
            .fetch(deviceIdField)

        return revokedDeviceIds.any { it != currentDeviceId }
    }

    override fun create(cfg: Configuration, session: NewSession) {
        val issuedAt = OffsetDateTime.ofInstant(session.issuedAt, ZoneOffset.UTC)
        val refreshExpiresAt = OffsetDateTime.ofInstant(session.refreshExpiresAt, ZoneOffset.UTC)
        DSL.using(cfg)
            .insertInto(DSL.table("sessions"))
            .set(DSL.field("id", UUID::class.java), session.id)
            .set(DSL.field("user_id", UUID::class.java), session.userId)
            .set(DSL.field("device_id", String::class.java), session.deviceId)
            .set(DSL.field("refresh_hash", String::class.java), session.refreshHash)
            .set(DSL.field("issued_at", OffsetDateTime::class.java), issuedAt)
            .set(DSL.field("refresh_expires_at", OffsetDateTime::class.java), refreshExpiresAt)
            .set(DSL.field("last_seen_at", OffsetDateTime::class.java), issuedAt)
            .set(DSL.field("user_agent", String::class.java), session.userAgent)
            .set(
                DSL.field("ip", String::class.java),
                DSL.field("CAST(? AS INET)", String::class.java, session.ip),
            )
            .execute()
    }

    override fun findActive(cfg: Configuration, sessionId: UUID): Session? {
        val idF = DSL.field("id", UUID::class.java)
        val userIdF = DSL.field("user_id", UUID::class.java)
        val deviceIdF = DSL.field("device_id", String::class.java)
        val refreshHashF = DSL.field("refresh_hash", String::class.java)
        val prevHashF = DSL.field("previous_refresh_hash", String::class.java)
        val issuedAtF = DSL.field("issued_at", OffsetDateTime::class.java)
        val refreshExpiresAtF = DSL.field("refresh_expires_at", OffsetDateTime::class.java)
        val lastSeenAtF = DSL.field("last_seen_at", OffsetDateTime::class.java)
        val revokedAtF = DSL.field("revoked_at", OffsetDateTime::class.java)
        val revokedReasonF = DSL.field("revoked_reason", String::class.java)


        return DSL.using(cfg)
            .select(idF, userIdF, deviceIdF, refreshHashF, prevHashF, issuedAtF,
                refreshExpiresAtF, lastSeenAtF, revokedAtF, revokedReasonF)
            .from(DSL.table("sessions"))
            .where(
                idF.eq(sessionId)
                    .and(revokedAtF.isNull)
                    .and(refreshExpiresAtF.gt(DSL.currentOffsetDateTime())),
            )
            .fetchOne()
            ?.let { r ->
                Session(
                    id = r.get(idF),
                    userId = r.get(userIdF),
                    deviceId = r.get(deviceIdF),
                    refreshHash = r.get(refreshHashF),
                    previousRefreshHash = r.get(prevHashF),
                    issuedAt = r.get(issuedAtF).toInstant(),
                    refreshExpiresAt = r.get(refreshExpiresAtF).toInstant(),
                    lastSeenAt = r.get(lastSeenAtF).toInstant(),
                    revokedAt = r.get(revokedAtF)?.toInstant(),
                    revokedReason = r.get(revokedReasonF),
                )
            }
    }

    override fun touchLastSeen(cfg: Configuration, sessionId: UUID) {
        DSL.using(cfg)
            .update(DSL.table("sessions"))
            .set(DSL.field("last_seen_at", OffsetDateTime::class.java), DSL.currentOffsetDateTime())
            .where(DSL.field("id", UUID::class.java).eq(sessionId))
            .execute()
    }
}
