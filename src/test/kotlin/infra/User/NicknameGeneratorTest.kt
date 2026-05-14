package infra.User

import com.example.infra.User.NicknameGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class NicknameGeneratorTest {
    @Test
    fun `is deterministic for same ugs sub`() {
        val gen = NicknameGenerator()
        assertEquals(gen.fromUgsSub("abc"), gen.fromUgsSub("abc"))
    }

    @Test
    fun `different ugs subs produce different nicknames`() {
        val gen = NicknameGenerator()
        assertNotEquals(gen.fromUgsSub("abc"), gen.fromUgsSub("xyz"))
    }

    @Test
    fun `format is Player_ plus 6 hex chars`() {
        val nick = NicknameGenerator().fromUgsSub("anything")
        assertTrue(nick.startsWith("Player_"))
        assertEquals(13, nick.length)
        assertTrue(nick.substring(7).all { it in '0'..'9' || it in 'a'..'f' })
    }
}
