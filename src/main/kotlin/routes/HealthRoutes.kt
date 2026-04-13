package com.example.routes

import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes() {
    get {
        call.respondText("Trinity Server Alive")
    }
}
