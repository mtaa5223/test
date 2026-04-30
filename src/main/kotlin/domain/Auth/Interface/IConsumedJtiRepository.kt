package com.example.domain.Auth.Interface

import org.jooq.Configuration
import java.time.Instant

interface IConsumedJtiRepository {
    fun consume(cfg: Configuration, jti: String, sub: String, expiresAt: Instant)
}
