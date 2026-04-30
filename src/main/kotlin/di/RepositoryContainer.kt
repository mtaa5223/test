package com.example.di

import com.example.domain.Auth.Interface.IConsumedJtiRepository
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.infra.Auth.PostgresConsumedJtiRepository
import com.example.infra.Signin.PostgresSignInCommandHandler

open class RepositoryContainer {
    open val signInCommandHandler: ISignInCommandHandler = PostgresSignInCommandHandler()
    open val consumedJtiRepository: IConsumedJtiRepository = PostgresConsumedJtiRepository()
}
