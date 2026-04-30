package com.example.infra.Signin

import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.domain.User.Result.SignInUserResult
import org.jooq.Configuration
import org.jooq.impl.DSL
import java.util.UUID

class PostgresSignInCommandHandler : ISignInCommandHandler {
    override fun execute(cfg: Configuration, command: SignInUserCommand): SignInUserResult {
        val record = DSL.using(cfg)
            .fetchOne(
                "INSERT INTO users (ugs_sub) VALUES (?) " +
                    "ON CONFLICT (ugs_sub) DO UPDATE SET ugs_sub = EXCLUDED.ugs_sub " +
                    "RETURNING id, (xmax = 0) AS is_new",
                command.ugsSub,
            )!!
        return SignInUserResult(
            userId = record.get("id", UUID::class.java),
            isNew = record.get("is_new", Boolean::class.java),
        )
    }
}
