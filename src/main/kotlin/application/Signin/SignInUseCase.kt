package com.example.application.Signin

import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler

class SignInUseCase(
    private val signInCommandHandler: ISignInCommandHandler,
)
{
    fun signIn(playerId: String) {
        signInCommandHandler.executeAsync(SignInUserCommand(playerId))
    }
}
