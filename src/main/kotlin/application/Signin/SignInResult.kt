package com.example.application.Signin

import com.example.domain.Auth.IssuedTokens
import java.util.UUID

data class SignInResult(
    val userId: UUID,
    val nickname: String,
    val isNew: Boolean,
    val replacedOtherDevice: Boolean,
    val tokens: IssuedTokens,
)
