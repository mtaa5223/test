package com.example.infra.User

import java.security.MessageDigest

class NicknameGenerator {
    fun fromUgsSub(ugsSub: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(ugsSub.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString(separator = "") { "%02x".format(it) }
        return "Player_" + hex.substring(0, 6)
    }
}
