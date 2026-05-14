package infra.Auth

import com.example.infra.Auth.RefreshTokenGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RefreshTokenGeneratorTest {
    @Test
    fun `tokens are unique`() {
        val gen = RefreshTokenGenerator()
        assertNotEquals(gen.newToken(), gen.newToken())
    }

    @Test
    fun `hash is deterministic and 64 hex chars`() {
        val gen = RefreshTokenGenerator()
        val token = gen.newToken()
        assertEquals(gen.hash(token), gen.hash(token))
        assertEquals(64, gen.hash(token).length)
        assertTrue(gen.hash(token).all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `hash differs for different tokens`() {
        val gen = RefreshTokenGenerator()
        assertNotEquals(gen.hash("a"), gen.hash("b"))
    }
}
