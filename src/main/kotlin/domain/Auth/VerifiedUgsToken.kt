package com.example.domain.Auth

import java.time.Instant

data class VerifiedUgsToken(
    val sub: String, //식별자
    val jti: String,   //토큰 고유 식별자
    val expiresAt: Instant, //토큰 만료 시간
    val issuedAt: Instant, //토큰 발급 시간
)
