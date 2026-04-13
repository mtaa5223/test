package com.example.infra.Auth

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import java.net.URI
import java.util.concurrent.TimeUnit

class UgsJwtConfig(
    val issuer: String,
    val audience: String,
    jwksUrl: String,
) {
    val jwkProvider: JwkProvider = JwkProviderBuilder(URI(jwksUrl).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    companion object {
        fun from(application: Application): UgsJwtConfig {
            val cfg = application.environment.config.config("ugs")
            return UgsJwtConfig(
                issuer = cfg.property("issuer").getString(),
                audience = cfg.property("projectId").getString(),
                jwksUrl = cfg.property("jwksUrl").getString(),
            )
        }
    }
}
