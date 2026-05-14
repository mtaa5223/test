package com.example.domain.Auth

import java.time.Instant

data class IssuedTokens(
    val accessToken: String,
    val accessExpiresAt: Instant,
    val refreshToken: String,
    val refreshExpiresAt: Instant,
)
