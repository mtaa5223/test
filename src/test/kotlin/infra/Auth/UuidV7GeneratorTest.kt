package infra.Auth

import com.example.infra.Auth.UuidV7Generator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class UuidV7GeneratorTest {
    @Test
    fun `version is 7`() {
        val u = UuidV7Generator().next()
        assertEquals(7, u.version())
    }

    @Test
    fun `successive ids are sortable by time`() {
        val gen = UuidV7Generator()
        val a = gen.next()
        Thread.sleep(2)
        val b = gen.next()
        assertNotEquals(a, b)
        assertTrue(a.toString() < b.toString())
    }
}
