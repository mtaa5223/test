package com.example.domain.User.Interface

import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Result.SignInUserResult
import org.jooq.Configuration

interface ISignInCommandHandler {
    fun execute(cfg: Configuration, command: SignInUserCommand): SignInUserResult
}
