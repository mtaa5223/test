package com.example.di

import com.example.domain.Auth.Interface.IConsumedJtiRepository
import com.example.domain.Auth.Interface.ISessionRepository
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.infra.Auth.PostgresConsumedJtiRepository
import com.example.infra.Auth.PostgresSessionRepository
import com.example.infra.Signin.PostgresSignInCommandHandler

open class RepositoryContainer(infra: InfraContainer) {
    open val signInCommandHandler: ISignInCommandHandler =
        PostgresSignInCommandHandler(infra.uuidV7Generator, infra.nicknameGenerator)
    open val consumedJtiRepository: IConsumedJtiRepository = PostgresConsumedJtiRepository()
    open val sessionRepository: ISessionRepository = PostgresSessionRepository()
}
