package com.example

import com.example.di.AppGraph
import com.example.plugins.configureAuthentication
import com.example.routes.configureRouting
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    dotenv {
        ignoreIfMissing = true
        systemProperties = true
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val graph = AppGraph(this)

    configureAuthentication()
    configureRouting(graph.useCases)
}
