package com.example.di

import com.example.infra.database.DataFactory
import io.ktor.server.application.Application

open class InfraContainer(application: Application) {
    init {
        DataFactory.init(application.environment.config)
    }
}
