package com.example.di

import com.example.application.Signin.SignInUseCase
import com.example.application.Upgrade.UpgradeUseCase

class UseCaseContainer(repos: RepositoryContainer) {
    val upgradeUseCase = UpgradeUseCase(
        repos.productionRepository,
        repos.upgradeQueryRepository,
        repos.productionSpecRegistry,
    )
    val signInUseCase = SignInUseCase(repos.signInCommandHandler)
}
