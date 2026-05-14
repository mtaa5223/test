package support

import io.ktor.http.ContentType
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.net.ServerSocket
import java.util.Base64

/**
 * 테스트용 JWKS 엔드포인트 서버.
 * 등록된 공개키들을 /.well-known/jwks.json 으로 노출한다.
 */
class MockJwksServer private constructor(
    private val engine: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>,
    val port: Int,
) {
    val jwksUrl: String get() = "http://127.0.0.1:$port/.well-known/jwks.json"

    fun stop() {
        engine.stop(100, 200)
    }

    companion object {
        fun start(keys: List<RsaTestKeyPair>): MockJwksServer {
            val port = freePort()
            val jwksJson = buildJwksJson(keys)
            val engine = embeddedServer(Netty, port = port) {
                routing {
                    get("/.well-known/jwks.json") {
                        call.respondText(jwksJson, ContentType.Application.Json)
                    }
                }
            }
            engine.start(wait = false)
            return MockJwksServer(engine, port)
        }

        private fun freePort(): Int = ServerSocket(0).use { it.localPort }

        private fun buildJwksJson(keys: List<RsaTestKeyPair>): String {
            val keyEntries = keys.joinToString(",") { kp ->
                val n = base64UrlUnsigned(kp.publicKey.modulus.toByteArray())
                val e = base64UrlUnsigned(kp.publicKey.publicExponent.toByteArray())
                """{"kty":"RSA","alg":"RS256","use":"sig","kid":"${kp.kid}","n":"$n","e":"$e"}"""
            }
            return """{"keys":[$keyEntries]}"""
        }

        private fun base64UrlUnsigned(bytes: ByteArray): String {
            // BigInteger.toByteArray() 는 부호 비트 때문에 선두에 0x00 이 붙을 수 있다. 제거.
            val unsigned = if (bytes.size > 1 && bytes[0] == 0.toByte()) bytes.copyOfRange(1, bytes.size) else bytes
            return Base64.getUrlEncoder().withoutPadding().encodeToString(unsigned)
        }
    }
}
