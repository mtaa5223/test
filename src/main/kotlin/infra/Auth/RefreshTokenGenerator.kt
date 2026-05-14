package com.example.infra.Auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class RefreshTokenGenerator(
    private val random: SecureRandom = SecureRandom(),
) {
    fun newToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
