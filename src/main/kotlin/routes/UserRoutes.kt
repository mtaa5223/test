package com.example.routes

import com.example.application.Signin.SignInUseCase
import com.example.domain.Auth.JtiAlreadyConsumedException
import com.example.infra.Auth.UgsPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.userRoutes(signInUseCase: SignInUseCase) {
    authenticate("ugs") {
        post("/user") {
            val token = call.principal<UgsPrincipal>()!!.token
            try {
                val result = signInUseCase.signIn(token)
                call.respondText(
                    """{"userId":"${result.userId}","isNew":${result.isNew}}""",
                    ContentType.Application.Json,
                    HttpStatusCode.OK,
                )
            } catch (e: JtiAlreadyConsumedException) {
                call.respondText(
                    """{"error":"TOKEN_REPLAYED"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.Unauthorized,
                )
            }
        }
    }
}
