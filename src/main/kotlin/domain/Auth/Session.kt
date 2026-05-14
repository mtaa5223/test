package com.example.domain.Auth

import java.time.Instant
import java.util.UUID

data class Session(
    val id: UUID,
    val userId: UUID,
    val deviceId: String,
    val refreshHash: String,
    val previousRefreshHash: String?,
    val issuedAt: Instant,
    val refreshExpiresAt: Instant,
    val lastSeenAt: Instant,
    val revokedAt: Instant?,
    val revokedReason: String?,
)

data class NewSession(
    val id: UUID,
    val userId: UUID,
    val deviceId: String,
    val refreshHash: String,
    val issuedAt: Instant,
    val refreshExpiresAt: Instant,
    val userAgent: String?,
    val ip: String?,
)
