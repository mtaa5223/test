package com.example.plugins

import com.example.infra.Auth.UgsJwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureAuthentication() {
    val ugs = UgsJwtConfig.from(this)
    install(Authentication) {
        jwt("ugs") {
            realm = "trinity"
            verifier(ugs.jwkProvider, ugs.issuer) {
                withAudience(ugs.audience)
                acceptLeeway(5)
            }
            validate { cred ->
                JWTPrincipal(cred.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
