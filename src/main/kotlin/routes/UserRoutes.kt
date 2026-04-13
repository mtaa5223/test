package com.example.routes

import com.example.application.Signin.SignInUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.userRoutes(signInUseCase: SignInUseCase) {
    authenticate("ugs") {
        post("/user") {
            val playerId = call.principal<JWTPrincipal>()!!.payload.subject
            signInUseCase.signIn(playerId)
            call.respond(HttpStatusCode.OK)
        }
    }
}
