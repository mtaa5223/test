package com.example.di

import com.example.infra.database.DataFactory
import io.ktor.server.application.Application

class AppGraph(application: Application) {
    val infra = InfraContainer(application)
    val repositories = RepositoryContainer(infra)
    val useCases = UseCaseContainer(DataFactory.context, infra, repositories)
}
