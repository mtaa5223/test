package com.example.application.Signin

import com.example.domain.Auth.Interface.IConsumedJtiRepository
import com.example.domain.Auth.VerifiedUgsToken
import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.domain.User.Result.SignInUserResult
import org.jooq.DSLContext

class SignInUseCase(
    private val dsl: DSLContext,
    private val consumedJtiRepository: IConsumedJtiRepository,
    private val signInCommandHandler: ISignInCommandHandler,
) {
    fun signIn(token: VerifiedUgsToken): SignInUserResult {
        return dsl.transactionResult { cfg ->
            consumedJtiRepository.consume(cfg, token.jti, token.sub, token.expiresAt)
            signInCommandHandler.execute(cfg, SignInUserCommand(token.sub))
        }
    }
}
