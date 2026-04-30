package com.example.di

import com.example.application.Signin.SignInUseCase
import org.jooq.DSLContext

class UseCaseContainer(dsl: DSLContext, repos: RepositoryContainer) {
    val signInUseCase = SignInUseCase(dsl, repos.consumedJtiRepository, repos.signInCommandHandler)
}
