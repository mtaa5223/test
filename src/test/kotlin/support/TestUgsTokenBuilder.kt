package support

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * 테스트용 UGS JWT 토큰 발급기.
 * RsaTestKeyPair 의 개인키로 서명한다. 같은 키쌍의 공개키가 MockJwksServer 에 등록되어 있어야 검증 통과.
 */
class TestUgsTokenBuilder(
    private val keyPair: RsaTestKeyPair,
    val defaults: Defaults = Defaults(),
) {
    data class Defaults(
        val issuer: String = "https://player-auth.services.api.unity.com",
        val projectId: String = "test-pid-d044100d",
        val envId: String = "test-eid-dc5fbe07",
        val sub: String = "test-sub-Qpp",
        val signInProvider: String = "google-play-games",
        val tokenType: String = "authentication",
        val identityDomainId: String = "test-idd-23583b94",
        val envName: String = "production",
        val version: String = "1",
        val ttlSeconds: Long = 3600,
    )

    fun valid(
        sub: String = defaults.sub,
        jti: String = UUID.randomUUID().toString(),
        signInProvider: String? = defaults.signInProvider,
        issuer: String = defaults.issuer,
        audience: List<String> = defaultAudience(),
        tokenType: String? = defaults.tokenType,
        version: String? = defaults.version,
        projectId: String? = defaults.projectId,
        iat: Instant = Instant.now(),
        nbf: Instant = iat,
        exp: Instant = iat.plusSeconds(defaults.ttlSeconds),
        algorithm: Algorithm = Algorithm.RSA256(keyPair.publicKey, keyPair.privateKey),
        kid: String? = keyPair.kid,
    ): String {
        val builder = JWT.create()
            .withIssuer(issuer)
            .withSubject(sub)
            .withJWTId(jti)
            .withAudience(*audience.toTypedArray())
            .withIssuedAt(Date.from(iat))
            .withNotBefore(Date.from(nbf))
            .withExpiresAt(Date.from(exp))
        if (kid != null) builder.withKeyId(kid)
        if (signInProvider != null) builder.withClaim("sign_in_provider", signInProvider)
        if (tokenType != null) builder.withClaim("token_type", tokenType)
        if (version != null) builder.withClaim("version", version)
        if (projectId != null) builder.withClaim("project_id", projectId)
        return builder.sign(algorithm)
    }

    private fun defaultAudience(): List<String> = listOf(
        "idd:${defaults.identityDomainId}",
        "envName:${defaults.envName}",
        "envId:${defaults.envId}",
        "upid:${defaults.projectId}",
    )

    // ── 변형 헬퍼들 ──

    fun expired(): String = valid(
        iat = Instant.now().minusSeconds(7200),
        nbf = Instant.now().minusSeconds(7200),
        exp = Instant.now().minusSeconds(120),
    )

    /** exp 가 leeway(60s) 안의 과거 — boundary 통과 케이스 */
    fun expiredWithinLeeway(): String = valid(
        iat = Instant.now().minusSeconds(3700),
        nbf = Instant.now().minusSeconds(3700),
        exp = Instant.now().minusSeconds(59),
    )

    /** exp 가 leeway(60s) 밖의 과거 — boundary 거부 케이스 */
    fun expiredPastLeeway(): String = valid(
        iat = Instant.now().minusSeconds(3700),
        nbf = Instant.now().minusSeconds(3700),
        exp = Instant.now().minusSeconds(120),
    )

    fun notYetValid(): String = valid(
        // nbf 가 leeway 초과 미래
        iat = Instant.now(),
        nbf = Instant.now().plusSeconds(300),
        exp = Instant.now().plusSeconds(3600),
    )

    fun wrongIssuer(): String = valid(issuer = "https://evil.example.com")

    fun missingUpid(): String = valid(
        audience = listOf(
            "idd:${defaults.identityDomainId}",
            "envName:${defaults.envName}",
            "envId:${defaults.envId}",
            // upid 빠짐
        ),
    )

    fun missingEnvId(): String = valid(
        audience = listOf(
            "idd:${defaults.identityDomainId}",
            "envName:${defaults.envName}",
            // envId 빠짐
            "upid:${defaults.projectId}",
        ),
    )

    fun emptyAudience(): String = valid(audience = emptyList())

    fun wrongTokenType(): String = valid(tokenType = "refresh")

    fun missingTokenType(): String = valid(tokenType = null)

    fun anonymous(): String = valid(signInProvider = "anonymous")

    fun missingSignInProvider(): String = valid(signInProvider = null)

    /** 다른 RSA 키쌍으로 서명 (kid 는 우리 것으로 위장) */
    fun signedWithDifferentKey(): String {
        val attacker = RsaTestKeys.generate(kid = keyPair.kid)
        return valid(
            algorithm = Algorithm.RSA256(attacker.publicKey, attacker.privateKey),
        )
    }

    /** JWKS 에 등록되지 않은 kid */
    fun unknownKid(): String = valid(kid = "unknown-kid-xxx")

    /** kid 헤더가 아예 없는 토큰 */
    fun nullKid(): String = valid(kid = null)

    /** alg=none 토큰 (서명 없는 토큰을 통과시키려는 시도) */
    fun algNone(): String = JWT.create()
        .withIssuer(defaults.issuer)
        .withSubject(defaults.sub)
        .withJWTId(UUID.randomUUID().toString())
        .withAudience(*defaultAudience().toTypedArray())
        .withIssuedAt(Date.from(Instant.now()))
        .withNotBefore(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
        .withKeyId(keyPair.kid)
        .withClaim("sign_in_provider", defaults.signInProvider)
        .withClaim("token_type", defaults.tokenType)
        .sign(Algorithm.none())

    /** 대칭키 (HS256) 로 위장 시도 */
    fun hmacAlgorithm(): String {
        val builder = JWT.create()
            .withIssuer(defaults.issuer)
            .withSubject(defaults.sub)
            .withJWTId(UUID.randomUUID().toString())
            .withAudience(*defaultAudience().toTypedArray())
            .withIssuedAt(Date.from(Instant.now()))
            .withNotBefore(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .withKeyId(keyPair.kid)
            .withClaim("sign_in_provider", defaults.signInProvider)
            .withClaim("token_type", defaults.tokenType)
        return builder.sign(Algorithm.HMAC256("attacker-secret"))
    }
}
