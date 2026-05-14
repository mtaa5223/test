package com.example.application.Signin

import com.example.domain.Auth.IssuedTokens
import com.example.domain.Auth.Interface.IConsumedJtiRepository
import com.example.domain.Auth.Interface.ISessionRepository
import com.example.domain.Auth.NewSession
import com.example.domain.Auth.VerifiedUgsToken
import com.example.domain.User.Command.SignInUserCommand
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.infra.Auth.AppJwt.AppJwtSigner
import com.example.infra.Auth.RefreshTokenGenerator
import com.example.infra.Auth.UuidV7Generator
import org.jooq.DSLContext
import java.time.Clock
import java.time.Instant

class SignInUseCase(
    private val dsl: DSLContext,
    private val consumedJtiRepository: IConsumedJtiRepository,
    private val signInCommandHandler: ISignInCommandHandler,
    private val sessionRepository: ISessionRepository,
    private val appJwtSigner: AppJwtSigner,
    private val refreshTokenGenerator: RefreshTokenGenerator,
    private val uuidV7Generator: UuidV7Generator,
    private val refreshTtlSeconds: Long,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun signIn(
        token: VerifiedUgsToken,
        deviceId: String,
        userAgent: String?,
        ip: String?,
    ): SignInResult {
        return dsl.transactionResult { cfg ->
            consumedJtiRepository.consume(cfg, token.jti, token.sub, token.expiresAt)
            val user = signInCommandHandler.execute(cfg, SignInUserCommand(token.sub))

            val replacedOtherDevice =
                sessionRepository.revokeActiveForUser(cfg, user.userId, deviceId)

            val now = Instant.now(clock)
            val refreshToken = refreshTokenGenerator.newToken()
            val refreshExpiresAt = now.plusSeconds(refreshTtlSeconds)
            val sessionId = uuidV7Generator.next()

            sessionRepository.create(
                cfg,
                NewSession(
                    id = sessionId,
                    userId = user.userId,
                    deviceId = deviceId,
                    refreshHash = refreshTokenGenerator.hash(refreshToken),
                    issuedAt = now,
                    refreshExpiresAt = refreshExpiresAt,
                    userAgent = userAgent,
                    ip = ip,
                ),
            )

            val access = appJwtSigner.sign(user.userId, sessionId)

            SignInResult(
                userId = user.userId,
                nickname = user.nickname,
                isNew = user.isNew,
                replacedOtherDevice = replacedOtherDevice,
                tokens = IssuedTokens(
                    accessToken = access.token,
                    accessExpiresAt = access.expiresAt,
                    refreshToken = refreshToken,
                    refreshExpiresAt = refreshExpiresAt,
                ),
            )
        }
    }
}
