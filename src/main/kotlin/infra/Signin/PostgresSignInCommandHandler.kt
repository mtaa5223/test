package com.example.infra.Signin

import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.domain.User.Result.SignInUserResult
import com.example.infra.Auth.UuidV7Generator
import com.example.infra.User.NicknameGenerator
import org.jooq.Configuration
import org.jooq.impl.DSL
import java.util.UUID

class PostgresSignInCommandHandler(
    private val uuidV7Generator: UuidV7Generator,
    private val nicknameGenerator: NicknameGenerator,
) : ISignInCommandHandler {
    override fun execute(cfg: Configuration, command: SignInUserCommand): SignInUserResult {
        val ctx = DSL.using(cfg)

        val idF = DSL.field("id", UUID::class.java)
        val ugsSubF = DSL.field("ugs_sub", String::class.java)
        val nicknameF = DSL.field("nickname", String::class.java)
        val usersTable = DSL.table("users")

        val proposedId = uuidV7Generator.next()
        val proposedNickname = nicknameGenerator.fromUgsSub(command.ugsSub)

        val inserted = ctx
            .insertInto(usersTable)
            .columns(idF, ugsSubF, nicknameF)
            .values(proposedId, command.ugsSub, proposedNickname)
            .onConflict(ugsSubF)
            .doNothing()
            .returning(idF, nicknameF)
            .fetchOne()

        if (inserted != null) {
            return SignInUserResult(
                userId = inserted.get(idF),
                nickname = inserted.get(nicknameF),
                isNew = true,
            )
        }

        val existing = ctx
            .select(idF, nicknameF)
            .from(usersTable)
            .where(ugsSubF.eq(command.ugsSub))
            .fetchOne()!!

        return SignInUserResult(
            userId = existing.get(idF),
            nickname = existing.get(nicknameF),
            isNew = false,
        )
    }
}
