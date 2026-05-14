package com.example.di

import com.example.application.Signin.SignInUseCase
import org.jooq.DSLContext

class UseCaseContainer(
    dsl: DSLContext,
    infra: InfraContainer,
    repos: RepositoryContainer,
) {
    val signInUseCase = SignInUseCase(
        dsl = dsl,
        consumedJtiRepository = repos.consumedJtiRepository,
        signInCommandHandler = repos.signInCommandHandler,
        sessionRepository = repos.sessionRepository,
        appJwtSigner = infra.appJwtSigner,
        refreshTokenGenerator = infra.refreshTokenGenerator,
        uuidV7Generator = infra.uuidV7Generator,
        refreshTtlSeconds = infra.appJwtProperties.refreshTtlSeconds,
    )
}
