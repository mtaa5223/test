package com.example.infra.Auth

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import java.net.URI
import java.util.concurrent.TimeUnit

class UgsJwtConfig(props: UgsProperties) {
    val issuer: String = props.issuer
    val upidAudience: String = "upid:${props.projectId}"
    val envIdAudience: String = "envId:${props.envId}"

    val jwkProvider: JwkProvider = JwkProviderBuilder(URI(props.jwksUrl).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
}
