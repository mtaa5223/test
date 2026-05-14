package com.example.domain.Auth.Interface

import com.example.domain.Auth.NewSession
import com.example.domain.Auth.Session
import org.jooq.Configuration
import java.util.UUID

interface ISessionRepository {
    fun revokeActiveForUser(
        cfg: Configuration,
        userId: UUID,
        currentDeviceId: String,
    ): Boolean

    fun create(cfg: Configuration, session: NewSession)

    fun findActive(cfg: Configuration, sessionId: UUID): Session?

    fun touchLastSeen(cfg: Configuration, sessionId: UUID)
}
