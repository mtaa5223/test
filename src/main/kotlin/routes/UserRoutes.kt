package com.example.routes

import com.example.application.Signin.SignInUseCase
import com.example.domain.Auth.JtiAlreadyConsumedException
import com.example.infra.Auth.UgsPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.request.userAgent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
private data class SignInRequest(val device_id: String)

@Serializable
private data class SignInResponse(
    val userId: String,
    val isNew: Boolean,
    val nickname: String,
    val accessToken: String,
    val accessExpiresAt: String,
    val refreshToken: String,
    val refreshExpiresAt: String,
    val replacedOtherDevice: Boolean,
)

@Serializable
private data class ErrorResponse(val error: String)

fun Route.userRoutes(signInUseCase: SignInUseCase) {
    authenticate("ugs") {
        post("/user") {
            val token = call.principal<UgsPrincipal>()!!.token
            val req = call.receive<SignInRequest>()
            if (req.device_id.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("device_id required"))
                return@post
            }

            val userAgent = call.request.userAgent()
            val ip = clientIp(call.request.headers["X-Forwarded-For"], call.request.origin.remoteHost)

            try {
                val result = signInUseCase.signIn(token, req.device_id, userAgent, ip)
                call.respond(
                    HttpStatusCode.OK,
                    SignInResponse(
                        userId = result.userId.toString(),
                        isNew = result.isNew,
                        nickname = result.nickname,
                        accessToken = result.tokens.accessToken,
                        accessExpiresAt = result.tokens.accessExpiresAt.toString(),
                        refreshToken = result.tokens.refreshToken,
                        refreshExpiresAt = result.tokens.refreshExpiresAt.toString(),
                        replacedOtherDevice = result.replacedOtherDevice,
                    ),
                )
            } catch (e: JtiAlreadyConsumedException) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("TOKEN_REPLAYED"))
            }
        }
    }
}

private fun clientIp(forwardedFor: String?, remoteHost: String): String? {
    val first = forwardedFor?.split(',')?.firstOrNull()?.trim()
    return when {
        !first.isNullOrBlank() -> first
        remoteHost.isNotBlank() -> remoteHost
        else -> null
    }
}
