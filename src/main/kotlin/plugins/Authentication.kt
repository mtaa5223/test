package com.example.plugins

import com.example.domain.Auth.VerifiedUgsToken
import com.example.infra.Auth.UgsJwtConfig
import com.example.infra.Auth.UgsPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureAuthentication() {
    val ugs = UgsJwtConfig.from(this)
    install(Authentication) {
        jwt("ugs") {
            realm = "trinity"
            verifier(ugs.jwkProvider, ugs.issuer) {
                withAudience(ugs.audience)
                acceptLeeway(300)
            }
            validate { cred ->
                val sub = cred.payload.subject
                val jti = cred.payload.id
                val exp = cred.payload.expiresAt?.toInstant()
                val iat = cred.payload.issuedAt?.toInstant()
                if (sub.isNullOrBlank() || jti.isNullOrBlank() || exp == null || iat == null) {
                    return@validate null
                }
                UgsPrincipal(
                    VerifiedUgsToken(
                        sub = sub,
                        jti = jti,
                        expiresAt = exp,
                        issuedAt = iat,
                    )
                )
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
