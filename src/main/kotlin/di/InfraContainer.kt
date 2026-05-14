package com.example.di

import com.example.infra.Auth.AppJwt.AppJwtConfig
import com.example.infra.Auth.AppJwt.AppJwtProperties
import com.example.infra.Auth.AppJwt.AppJwtSigner
import com.example.infra.Auth.RefreshTokenGenerator
import com.example.infra.Auth.UgsJwtConfig
import com.example.infra.Auth.UgsProperties
import com.example.infra.Auth.UuidV7Generator
import com.example.infra.User.NicknameGenerator
import com.example.infra.database.DataFactory
import io.ktor.server.application.Application

open class InfraContainer(application: Application) {
    init {
        DataFactory.init(application.environment.config)
    }

    open val ugsProperties: UgsProperties = UgsProperties.from(application)
    open val ugsJwtConfig: UgsJwtConfig = UgsJwtConfig(ugsProperties)

    open val appJwtProperties: AppJwtProperties = AppJwtProperties.from(application)
    open val appJwtConfig: AppJwtConfig = AppJwtConfig(appJwtProperties)
    open val appJwtSigner: AppJwtSigner = AppJwtSigner(appJwtConfig)

    open val uuidV7Generator: UuidV7Generator = UuidV7Generator()
    open val nicknameGenerator: NicknameGenerator = NicknameGenerator()
    open val refreshTokenGenerator: RefreshTokenGenerator = RefreshTokenGenerator()
}
