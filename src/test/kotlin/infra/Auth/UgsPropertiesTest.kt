package infra.Auth

import com.example.infra.Auth.UgsProperties
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UgsPropertiesTest {

    private fun config(vararg entries: Pair<String, String>): ApplicationConfig =
        MapApplicationConfig(*entries)

    private val fullConfig = arrayOf(
        "ugs.issuer" to "https://issuer.example",
        "ugs.projectId" to "proj-1",
        "ugs.envId" to "env-1",
        "ugs.jwksUrl" to "https://jwks.example/.well-known/jwks.json",
    )

    // ugs.* 4개 키가 모두 있을 때 정상 객체로 매핑되는지 확인.
    @Test
    fun `from loads all four keys from config`() {
        val props = UgsProperties.from(config(*fullConfig))

        assertEquals("https://issuer.example", props.issuer)
        assertEquals("proj-1", props.projectId)
        assertEquals("env-1", props.envId)
        assertEquals("https://jwks.example/.well-known/jwks.json", props.jwksUrl)
    }

    // issuer 누락 시 fail-fast — 잘못된 운영 환경(설정 빠짐)에서 조용히 기동되면 안 됨.
    @Test
    fun `from throws when issuer is missing`() {
        val entries = fullConfig.filterNot { it.first == "ugs.issuer" }.toTypedArray()
        assertFailsWith<Exception> { UgsProperties.from(config(*entries)) }
    }

    // projectId 누락 시 fail-fast (upid aud를 만들 수 없음).
    @Test
    fun `from throws when projectId is missing`() {
        val entries = fullConfig.filterNot { it.first == "ugs.projectId" }.toTypedArray()
        assertFailsWith<Exception> { UgsProperties.from(config(*entries)) }
    }

    // envId 누락 시 fail-fast (envId aud를 만들 수 없음).
    @Test
    fun `from throws when envId is missing`() {
        val entries = fullConfig.filterNot { it.first == "ugs.envId" }.toTypedArray()
        assertFailsWith<Exception> { UgsProperties.from(config(*entries)) }
    }

    // jwksUrl 누락 시 fail-fast (JWKS 조회처가 없으면 서명 검증 자체가 불가).
    @Test
    fun `from throws when jwksUrl is missing`() {
        val entries = fullConfig.filterNot { it.first == "ugs.jwksUrl" }.toTypedArray()
        assertFailsWith<Exception> { UgsProperties.from(config(*entries)) }
    }
}
