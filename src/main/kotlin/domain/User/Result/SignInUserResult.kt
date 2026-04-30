package com.example.domain.User.Result

import java.util.UUID

data class SignInUserResult(val userId: UUID, val isNew: Boolean)
