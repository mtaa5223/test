package infra.Auth

import com.example.plugins.configureUgsAuthentication
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import support.MockJwksServer
import support.RsaTestKeyPair
import support.RsaTestKeys
import support.TestUgsTokenBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UgsJwtAuthTest {
    private lateinit var keyPair: RsaTestKeyPair
    private lateinit var jwks: MockJwksServer
    private lateinit var tokens: TestUgsTokenBuilder

    @BeforeAll
    fun setUp() {
        keyPair = RsaTestKeys.generate()
        jwks = MockJwksServer.start(listOf(keyPair))
        tokens = TestUgsTokenBuilder(keyPair)
    }

    @AfterAll
    fun tearDown() {
        jwks.stop()
    }

    private fun ApplicationTestBuilder.installAuth() {
        environment {
            config = MapApplicationConfig(
                "ugs.issuer" to tokens.defaults.issuer,
                "ugs.projectId" to tokens.defaults.projectId,
                "ugs.envId" to tokens.defaults.envId,
                "ugs.jwksUrl" to jwks.jwksUrl,
            )
        }
        application {
            configureUgsAuthentication()
            routing {
                authenticate("ugs") {
                    get("/protected") { call.respondText("ok") }
                }
            }
        }
    }

    private suspend fun ApplicationTestBuilder.callProtected(
        authorization: String? = null,
    ): HttpStatusCode = client.get("/protected") {
        if (authorization != null) header(HttpHeaders.Authorization, authorization)
    }.status

    private suspend fun ApplicationTestBuilder.callWithBearer(token: String?): HttpStatusCode =
        callProtected(authorization = token?.let { "Bearer $it" })

    // ─────────── A. Smoke ───────────

    // 정상 토큰의 happy path. verifier + validator 통합이 깨지면 모든 게 깨짐 — 베이스라인.
    @Test
    fun `valid token returns 200`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.OK, callWithBearer(tokens.valid()))
    }

    // validator 통합점 smoke (명세 #2). validator 단위테스트가 통과해도
    // Authentication.kt 의 매핑/연결이 깨지면 이 테스트가 잡아줌.
    @Test
    fun `anonymous provider token returns 401 (validator integration)`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.anonymous()))
    }

    // ─────────── B. HTTP 헤더 ───────────

    // Authorization 헤더 자체가 없는 요청은 challenge 단계에서 401.
    @Test
    fun `missing Authorization header returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callProtected(authorization = null))
    }

    // Bearer 스킴은 맞지만 토큰이 비어있는 경우. ktor jwt 플러그인이 빈 토큰을 거부하는지.
    @Test
    fun `empty bearer token returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callProtected(authorization = "Bearer "))
    }

    // 다른 스킴(Basic 등)은 jwt 플러그인이 매칭조차 안 함 → 401.
    @Test
    fun `non-Bearer scheme returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callProtected(authorization = "Basic dXNlcjpwYXNz"))
    }

    // ─────────── C. 서명 / 알고리즘 ───────────

    // JWT 형식 자체가 깨진 입력. 파싱 단계에서 거부.
    @Test
    fun `malformed token returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer("not-a-jwt"))
    }

    // 다른 RSA 키쌍으로 서명한 토큰(우리 kid 위장). 서명 검증 실패 → 401.
    @Test
    fun `token signed with different key returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.signedWithDifferentKey()))
    }

    // 대칭키(HS256) 위장 시도. RS256 키만 등록된 JWKS 와 매칭 실패 → 401.
    @Test
    fun `HS256 algorithm spoofing returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.hmacAlgorithm()))
    }

    // alg=none 위장 시도. verifier 가 서명 없는 토큰을 절대 통과시키면 안 됨.
    @Test
    fun `alg none token returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.algNone()))
    }

    // ─────────── D. JWKS / kid ───────────

    // JWKS 에 등록되지 않은 kid → 공개키 조회 실패 → 401.
    @Test
    fun `unknown kid returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.unknownKid()))
    }

    // kid 헤더 자체가 없는 토큰. JwkProvider 가 어느 키로 검증할지 결정 못 함 → 401.
    @Test
    fun `missing kid header returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.nullKid()))
    }

    // ─────────── E. 시간 검증 (exp / nbf, leeway 60s) ───────────

    // exp 가 한참 과거(2분 전). leeway 와 무관하게 거부.
    @Test
    fun `expired token returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.expired()))
    }

    // exp 가 leeway(60s) 안의 과거(-59s). boundary 통과 케이스.
    @Test
    fun `expired within leeway returns 200 (boundary)`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.OK, callWithBearer(tokens.expiredWithinLeeway()))
    }

    // exp 가 leeway 밖의 과거(-120s). boundary 거부 케이스 — leeway 가 우연히 커지면 이 테스트가 잡음.
    @Test
    fun `expired past leeway returns 401 (boundary)`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.expiredPastLeeway()))
    }

    // nbf 가 leeway 초과 미래. 아직 유효하지 않은 토큰은 거부.
    @Test
    fun `nbf in future returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.notYetValid()))
    }

    // ─────────── F. 클레임 — iss / aud / token_type ───────────

    // 등록된 issuer 와 다른 iss → 401.
    @Test
    fun `wrong issuer returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.wrongIssuer()))
    }

    // 명세 #3: aud 매칭은 AND. upid 가 빠지면 거부.
    @Test
    fun `missing upid in aud returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.missingUpid()))
    }

    // 명세 #3: aud 매칭은 AND. envId 가 빠지면 거부.
    @Test
    fun `missing envId in aud returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.missingEnvId()))
    }

    // aud 자체가 빈 배열인 토큰 → 어느 audience 도 매칭 안 됨 → 401.
    @Test
    fun `empty aud returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.emptyAudience()))
    }

    // token_type 이 "authentication" 이 아닌 다른 값(refresh 등) → 401.
    @Test
    fun `wrong token_type returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.wrongTokenType()))
    }

    // token_type 클레임 자체가 누락 → 401.
    @Test
    fun `missing token_type returns 401`() = testApplication {
        installAuth()
        assertEquals(HttpStatusCode.Unauthorized, callWithBearer(tokens.missingTokenType()))
    }
}
