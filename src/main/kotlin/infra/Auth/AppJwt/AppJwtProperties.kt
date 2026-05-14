package com.example.infra.Auth.AppJwt

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

data class AppJwtProperties(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessTtlSeconds: Long,
    val refreshTtlSeconds: Long,
) {
    init {
        require(secret.isNotBlank()) { "appJwt.secret must be set (APP_JWT_SECRET)" }
        require(secret.length >= 32) { "appJwt.secret must be at least 32 chars" }
    }

    companion object {
        fun from(application: Application): AppJwtProperties =
            from(application.environment.config)

        fun from(config: ApplicationConfig): AppJwtProperties {
            val cfg = config.config("appJwt")
            return AppJwtProperties(
                secret = cfg.property("secret").getString(),
                issuer = cfg.property("issuer").getString(),
                audience = cfg.property("audience").getString(),
                accessTtlSeconds = cfg.property("accessTtlSeconds").getString().toLong(),
                refreshTtlSeconds = cfg.property("refreshTtlSeconds").getString().toLong(),
            )
        }
    }
}
