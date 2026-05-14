package com.example.infra.Auth.AppJwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.LoggerFactory
import java.security.SecureRandom

class AppJwtConfig(val props: AppJwtProperties) {
    private val effectiveSecret: String = resolveSecret(props.secret)

    val algorithm: Algorithm = Algorithm.HMAC256(effectiveSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(props.issuer)
        .withAudience(props.audience)
        .acceptLeeway(5)
        .build()

    private companion object {
        private val log = LoggerFactory.getLogger(AppJwtConfig::class.java)

        // APP_JWT_SECRET이 없거나 32자 미만이면 부팅을 막지 않고 임시 랜덤 키로 폴백.
        // 프로세스 재시작마다 키가 바뀌므로 그때 발급된 access token은 전부 무효가 된다.
        // 운영에선 반드시 APP_JWT_SECRET을 32자 이상으로 주입해 안정 키로 서명해야 한다.
        fun resolveSecret(configured: String): String {
            if (configured.length >= 32) return configured
            val fallback = ByteArray(48).also { SecureRandom().nextBytes(it) }
                .joinToString("") { "%02x".format(it) }
            log.warn(
                "APP_JWT_SECRET is missing or shorter than 32 chars; using an ephemeral " +
                    "in-memory key. Issued access tokens will be invalidated on restart."
            )
            return fallback
        }
    }
}
