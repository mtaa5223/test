package com.example.domain.Auth

class JtiAlreadyConsumedException(val jti: String) : RuntimeException("jti already consumed: $jti")
