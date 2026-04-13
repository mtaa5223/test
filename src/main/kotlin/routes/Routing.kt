package com.example.routes

import com.example.di.UseCaseContainer
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(useCases: UseCaseContainer) {
    routing {
        healthRoutes()
        userRoutes(useCases.signInUseCase)
    }
}
