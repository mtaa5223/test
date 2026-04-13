package com.example.di

import io.ktor.server.application.Application

class AppGraph(application: Application) {
    val infra = InfraContainer(application)
    val repositories = RepositoryContainer()
    val useCases = UseCaseContainer(repositories)
}
