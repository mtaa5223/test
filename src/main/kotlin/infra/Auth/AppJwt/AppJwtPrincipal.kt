package com.example.infra.Auth.AppJwt

import java.util.UUID

data class AppJwtPrincipal(
    val userId: UUID,
    val sessionId: UUID,
)
