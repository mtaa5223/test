package infra.Auth

import com.example.infra.Auth.UgsJwtConfig
import com.example.infra.Auth.UgsProperties
import kotlin.test.Test
import kotlin.test.assertEquals

class UgsJwtConfigTest {

    private fun props(
        issuer: String = "https://issuer.example",
        projectId: String = "p",
        envId: String = "e",
        jwksUrl: String = "https://jwks.example/.well-known/jwks.json",
    ) = UgsProperties(issuer, projectId, envId, jwksUrl)

    // upid aud 포맷이 "upid:{projectId}" 규약을 지키는지. UGS 토큰 스펙과 직결.
    @Test
    fun `upidAudience formats as upid prefix + projectId`() {
        val config = UgsJwtConfig(props(projectId = "proj-1"))
        assertEquals("upid:proj-1", config.upidAudience)
    }

    // envId aud 포맷이 "envId:{envId}" 규약을 지키는지.
    @Test
    fun `envIdAudience formats as envId prefix + envId`() {
        val config = UgsJwtConfig(props(envId = "env-9"))
        assertEquals("envId:env-9", config.envIdAudience)
    }

    // issuer는 가공 없이 props 값 그대로 노출돼야 함 (verifier에 그대로 전달되므로).
    @Test
    fun `issuer is passed through unchanged`() {
        val config = UgsJwtConfig(props(issuer = "https://issuer.example"))
        assertEquals("https://issuer.example", config.issuer)
    }
}
