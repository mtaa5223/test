package infra.Auth.AppJwt

import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.infra.Auth.AppJwt.AppJwtConfig
import com.example.infra.Auth.AppJwt.AppJwtProperties
import com.example.infra.Auth.AppJwt.AppJwtSigner
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppJwtSignerTest {
    private val secret = "a".repeat(64)
    private val baseProps = AppJwtProperties(
        secret = secret,
        issuer = "ant",
        audience = "trinity-game",
        accessTtlSeconds = 900,
        refreshTtlSeconds = 2592000,
    )

    @Test
    fun `sign and verify round-trip`() {
        val cfg = AppJwtConfig(baseProps)
        val signer = AppJwtSigner(cfg)
        val userId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()

        val signed = signer.sign(userId, sessionId)
        val decoded = cfg.verifier.verify(signed.token)

        assertEquals(userId.toString(), decoded.subject)
        assertEquals(sessionId.toString(), decoded.getClaim("sid").asString())
        assertEquals("ant", decoded.issuer)
        assertEquals(listOf("trinity-game"), decoded.audience)
    }

    @Test
    fun `expired token is rejected`() {
        val fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        val cfg = AppJwtConfig(baseProps)
        val pastSigner = AppJwtSigner(cfg, fixed)
        val signed = pastSigner.sign(UUID.randomUUID(), UUID.randomUUID())

        assertFailsWith<JWTVerificationException> {
            cfg.verifier.verify(signed.token)
        }
    }

    @Test
    fun `wrong issuer is rejected`() {
        val cfg = AppJwtConfig(baseProps)
        val signer = AppJwtSigner(cfg)
        val signed = signer.sign(UUID.randomUUID(), UUID.randomUUID())

        val wrongCfg = AppJwtConfig(baseProps.copy(issuer = "other"))
        assertFailsWith<JWTVerificationException> {
            wrongCfg.verifier.verify(signed.token)
        }
    }
}
