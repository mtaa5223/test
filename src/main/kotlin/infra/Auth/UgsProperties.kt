package com.example.infra.Auth

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

data class UgsProperties(
    val issuer: String,
    val projectId: String,
    val envId: String,
    val jwksUrl: String,
) {
    companion object {
        fun from(application: Application): UgsProperties =
            from(application.environment.config)

        fun from(config: ApplicationConfig): UgsProperties {
            val cfg = config.config("ugs")
              return UgsProperties(
                issuer = cfg.property("issuer").getString(),
                projectId = cfg.property("projectId").getString(),
                envId = cfg.property("envId").getString(),
                jwksUrl = cfg.property("jwksUrl").getString(),
            )
        }
    }
}
