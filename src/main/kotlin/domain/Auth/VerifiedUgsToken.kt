package com.example.domain.Auth

import java.time.Instant

data class VerifiedUgsToken(
    val sub: String,
    val jti: String,
    val expiresAt: Instant,
    val issuedAt: Instant,
)
