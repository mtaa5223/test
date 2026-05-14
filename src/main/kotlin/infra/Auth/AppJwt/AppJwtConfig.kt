package com.example.infra.Auth.AppJwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

class AppJwtConfig(val props: AppJwtProperties) {
    val algorithm: Algorithm = Algorithm.HMAC256(props.secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(props.issuer)
        .withAudience(props.audience)
        .acceptLeeway(5)
        .build()
}
