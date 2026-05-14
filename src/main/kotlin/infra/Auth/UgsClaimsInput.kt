package com.example.infra.Auth

import java.time.Instant

data class UgsClaimsInput(
    val sub: String?,
    val jti: String?,
    val expiresAt: Instant?,
    val issuedAt: Instant?,
    val signInProvider: String?,
)
