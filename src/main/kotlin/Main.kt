package com.example

import com.example.di.AppGraph
import com.example.plugins.configureAppAuthentication
import com.example.plugins.configureSerialization
import com.example.plugins.configureUgsAuthentication
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

    configureSerialization()
    configureUgsAuthentication(graph.infra.ugsJwtConfig)
    configureAppAuthentication(
        appJwtConfig = graph.infra.appJwtConfig,
        dsl = com.example.infra.database.DataFactory.context,
        sessionRepository = graph.repositories.sessionRepository,
    )
    configureRouting(graph.useCases)
}
