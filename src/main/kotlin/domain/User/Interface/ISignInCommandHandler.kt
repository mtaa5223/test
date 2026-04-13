package com.example.domain.User.Interface

import com.example.domain.User.Command.SignInUserCommand

interface ISignInCommandHandler {
    fun executeAsync(command: SignInUserCommand)
}
