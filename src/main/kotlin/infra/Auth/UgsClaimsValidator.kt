package com.example.infra.Auth

import com.example.domain.Auth.VerifiedUgsToken
import java.time.Clock
import java.time.Duration
import java.time.Instant

class UgsClaimsValidator(
    private val clock: Clock = Clock.systemUTC(),
    private val iatFutureLeeway: Duration = Duration.ofSeconds(60),
) {
    fun validate(input: UgsClaimsInput): VerifiedUgsToken? {
        val sub = input.sub ?: return null
        val jti = input.jti ?: return null
        val exp = input.expiresAt ?: return null
        val iat = input.issuedAt ?: return null

        val provider = input.signInProvider
        if (provider.isNullOrBlank()) return null
        if (provider == "anonymous") return null

        val now = Instant.now(clock)
        if (iat.isAfter(now.plus(iatFutureLeeway))) return null

        return VerifiedUgsToken(sub = sub, jti = jti, expiresAt = exp, issuedAt = iat)
    }
}
