package com.example

import com.example.di.AppGraph
import com.example.infra.database.DataFactory
import com.example.plugins.configureAppAuthentication
import com.example.plugins.configureSerialization
import com.example.plugins.configureUgsAuthentication
import com.example.routes.configureRouting
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.ServerReady
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

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

    val log = LoggerFactory.getLogger("com.example.Migrations")
    monitor.subscribe(ServerReady) {
        thread(name = "flyway-migrate", isDaemon = true) {
            try {
                log.info("Running Flyway migrations after server start...")
                DataFactory.runMigrations()
                log.info("Flyway migrations finished.")
            } catch (e: Throwable) {
                log.error("Flyway migration failed", e)
            }
        }
    }
}
