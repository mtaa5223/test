package com.example.plugins

import com.example.domain.Auth.Interface.ISessionRepository
import com.example.infra.Auth.AppJwt.AppJwtConfig
import com.example.infra.Auth.AppJwt.AppJwtPrincipal
import com.example.infra.Auth.UgsClaimsInput
import com.example.infra.Auth.UgsClaimsValidator
import com.example.infra.Auth.UgsJwtConfig
import com.example.infra.Auth.UgsPrincipal
import com.example.infra.Auth.UgsProperties
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.jooq.DSLContext
import java.util.UUID

fun Application.configureUgsAuthentication(
    ugsJwtConfig: UgsJwtConfig = UgsJwtConfig(UgsProperties.from(this)),
) {
    val validator = UgsClaimsValidator()
    authentication {
        jwt("ugs") {
            realm = "trinity"
            verifier(ugsJwtConfig.jwkProvider, ugsJwtConfig.issuer) {
                withAudience(ugsJwtConfig.upidAudience, ugsJwtConfig.envIdAudience)
                withClaim("token_type", "authentication")
                acceptLeeway(60)
            }
            validate { cred ->
                val input = UgsClaimsInput(
                    sub = cred.payload.subject,
                    jti = cred.payload.id,
                    expiresAt = cred.payload.expiresAt?.toInstant(),
                    issuedAt = cred.payload.issuedAt?.toInstant(),
                    signInProvider = cred.payload.getClaim("sign_in_provider").asString(),
                )
                validator.validate(input)?.let { UgsPrincipal(it) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

fun Application.configureAppAuthentication(
    appJwtConfig: AppJwtConfig,
    dsl: DSLContext,
    sessionRepository: ISessionRepository,
) {
    authentication {
        jwt("app") {
            realm = "trinitys"
            verifier(appJwtConfig.verifier)
            validate { cred ->
                val sub = cred.payload.subject ?: return@validate null
                val sid = cred.payload.getClaim("sid").asString() ?: return@validate null
                val userId = runCatching { UUID.fromString(sub) }.getOrNull() ?: return@validate null
                val sessionId = runCatching { UUID.fromString(sid) }.getOrNull() ?: return@validate null

                val session = sessionRepository.findActive(dsl.configuration(), sessionId)
                    ?: return@validate null
                if (session.userId != userId) return@validate null

                sessionRepository.touchLastSeen(dsl.configuration(), sessionId)
                AppJwtPrincipal(userId = userId, sessionId = sessionId)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
