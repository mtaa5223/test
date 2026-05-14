package infra.Auth

import com.example.infra.Auth.UgsClaimsInput
import com.example.infra.Auth.UgsClaimsValidator
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UgsClaimsValidatorTest {

    private val now: Instant = Instant.parse("2026-01-01T00:00:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
    private val validator = UgsClaimsValidator(clock)

    private fun input(
        sub: String? = "sub-1",
        jti: String? = "jti-1",
        expiresAt: Instant? = now.plusSeconds(3600),
        issuedAt: Instant? = now.minusSeconds(10),
        signInProvider: String? = "google-play-games",
    ) = UgsClaimsInput(sub, jti, expiresAt, issuedAt, signInProvider)

    // ─────────── happy path ───────────

    // 모든 필드가 정상일 때 VerifiedUgsToken으로 매핑되는지 검증.
    // sub/jti/exp/iat 4개 필드가 입력 그대로 도메인 객체에 실리는지 확인 (명세 #8).
    @Test
    fun `valid input returns VerifiedUgsToken with mapped fields`() {
        val result = validator.validate(input())

        assertNotNull(result)
        assertEquals("sub-1", result.sub)
        assertEquals("jti-1", result.jti)
        assertEquals(now.plusSeconds(3600), result.expiresAt)
        assertEquals(now.minusSeconds(10), result.issuedAt)
    }

    // ─────────── 필수 필드 누락 ───────────

    // sub 없으면 식별자가 없는 토큰이므로 거부.
    @Test
    fun `null sub returns null`() {
        assertNull(validator.validate(input(sub = null)))
    }

    // jti 없으면 replay 방어가 불가능하므로 거부.
    @Test
    fun `null jti returns null`() {
        assertNull(validator.validate(input(jti = null)))
    }

    // exp 없으면 만료 시점을 알 수 없으므로 거부.
    @Test
    fun `null expiresAt returns null`() {
        assertNull(validator.validate(input(expiresAt = null)))
    }

    // iat 없으면 발급 시점을 알 수 없으므로 거부.
    @Test
    fun `null issuedAt returns null`() {
        assertNull(validator.validate(input(issuedAt = null)))
    }

    // ─────────── sign_in_provider 정책 ───────────

    // sign_in_provider 클레임이 아예 없는 토큰 거부.
    @Test
    fun `null sign_in_provider returns null`() {
        assertNull(validator.validate(input(signInProvider = null)))
    }

    // 빈 문자열도 누락과 동일 취급.
    @Test
    fun `empty sign_in_provider returns null`() {
        assertNull(validator.validate(input(signInProvider = "")))
    }

    // 공백만 있는 값도 누락과 동일 취급 (isBlank 정책).
    @Test
    fun `whitespace-only sign_in_provider returns null`() {
        assertNull(validator.validate(input(signInProvider = "   ")))
    }

    // 명세 #2: anonymous 플레이어 토큰은 차단.
    @Test
    fun `anonymous sign_in_provider is rejected`() {
        assertNull(validator.validate(input(signInProvider = "anonymous")))
    }

    // ─────────── iat future leeway (60s) ───────────

    // 명세 #1: 클라이언트 시계가 살짝 빠른 정상 케이스. 30s 미래는 leeway 내라 통과.
    @Test
    fun `iat 30s in the future is accepted (within leeway)`() {
        assertNotNull(validator.validate(input(issuedAt = now.plusSeconds(30))))
    }

    // 명세 #1 경계값: 정확히 60s 미래도 통과해야 함 (leeway 포함 경계).
    @Test
    fun `iat exactly 60s in the future is accepted (boundary)`() {
        assertNotNull(validator.validate(input(issuedAt = now.plusSeconds(60))))
    }

    // 명세 #1 경계값: 61s 미래는 leeway 초과로 거부 — 회귀 방지의 핵심 케이스.
    @Test
    fun `iat 61s in the future is rejected (just past leeway)`() {
        assertNull(validator.validate(input(issuedAt = now.plusSeconds(61))))
    }

    // 시계 차이가 아닌 명백한 비정상 토큰. leeway가 우연히 커져도 잡히도록.
    @Test
    fun `iat far future is rejected`() {
        assertNull(validator.validate(input(issuedAt = now.plusSeconds(3600))))
    }
}
