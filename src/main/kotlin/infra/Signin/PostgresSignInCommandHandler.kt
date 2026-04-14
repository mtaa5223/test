package com.example.infra.Signin

import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler

class PostgresSignInCommandHandler : ISignInCommandHandler {
    override fun executeAsync(command: SignInUserCommand) {
        // TODO: 실제 DB 저장 로직 구현
    }
}
