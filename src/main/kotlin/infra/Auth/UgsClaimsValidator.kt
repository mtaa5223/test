package com.example.infra.Auth

import com.example.domain.Auth.VerifiedUgsToken
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant

class UgsClaimsValidator(
    private val clock: Clock = Clock.systemUTC(),
    private val iatFutureLeeway: Duration = Duration.ofSeconds(60),
) {
    private val log = LoggerFactory.getLogger(UgsClaimsValidator::class.java)

    fun validate(input: UgsClaimsInput): VerifiedUgsToken? {
        val sub = input.sub ?: run {
            log.warn("UGS reject: sub missing")
            return null
        }
        val jti = input.jti ?: run {
            log.warn("UGS reject: jti missing (sub=$sub)")
            return null
        }
        val exp = input.expiresAt ?: run {
            log.warn("UGS reject: exp missing (sub=$sub jti=$jti)")
            return null
        }
        val iat = input.issuedAt ?: run {
            log.warn("UGS reject: iat missing (sub=$sub jti=$jti)")
            return null
        }

        val provider = input.signInProvider
        if (provider.isNullOrBlank()) {
            log.warn("UGS reject: sign_in_provider blank (sub=$sub jti=$jti)")
            return null
        }
        if (provider == "anonymous") {
            log.warn("UGS reject: anonymous provider (sub=$sub jti=$jti)")
            return null
        }

        val now = Instant.now(clock)
        if (iat.isAfter(now.plus(iatFutureLeeway))) {
            log.warn("UGS reject: iat too far in future (iat=$iat now=$now sub=$sub jti=$jti)")
            return null
        }

        log.info("UGS accept: sub=$sub jti=$jti provider=$provider exp=$exp")
        return VerifiedUgsToken(sub = sub, jti = jti, expiresAt = exp, issuedAt = iat)
    }
}
