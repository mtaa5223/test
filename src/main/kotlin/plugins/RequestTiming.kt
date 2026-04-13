package com.example.plugins
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.uri

object RequestTiming {
    // plugins/RequestTiming.kt

    fun Application.installRequestTiming() {
        intercept(ApplicationCallPipeline.Monitoring) {
            val startTime = System.currentTimeMillis()

            proceed()

            val endTime = System.currentTimeMillis()
            val path = call.request.uri

            println("TOTAL REQUEST TIME [$path]: ${endTime - startTime} ms")
        }
    }
}
