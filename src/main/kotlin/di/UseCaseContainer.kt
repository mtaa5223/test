package com.example.di

import com.example.application.Signin.SignInUseCase

class UseCaseContainer(repos: RepositoryContainer) {
    val signInUseCase = SignInUseCase(repos.signInCommandHandler)
}
