package com.example.infra.Auth.AppJwt

import com.auth0.jwt.JWT
import java.time.Clock
import java.time.Instant
import java.util.Date
import java.util.UUID

data class SignedAccessToken(
    val token: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
)

class AppJwtSigner(
    private val config: AppJwtConfig,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun sign(userId: UUID, sessionId: UUID): SignedAccessToken {
        val now = Instant.now(clock)
        val exp = now.plusSeconds(config.props.accessTtlSeconds)
        val token = JWT.create()
            .withIssuer(config.props.issuer)
            .withAudience(config.props.audience)
            .withSubject(userId.toString())
            .withClaim("sid", sessionId.toString())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(exp))
            .sign(config.algorithm)
        return SignedAccessToken(token = token, issuedAt = now, expiresAt = exp)
    }
}
