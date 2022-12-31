package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.commons.security.auth.isExpired
import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.Login
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val sessionService: SessionService,
    private val emailService: EmailService,
    @Value("\${customer-auth-signing-secret}")
    private val secret: String,
    @Value("\${customer-auth-expiration-token}")
    private val expirationAuthToken: String
) {

    fun authenticate(device: Device, login: Login): Authentication {
        logger.info("Authenticating user ${login.email}")

        val user = userRepository.findByEmail(login.email) ?: throw UserNotFoundException()

        if (EncryptUtils.validatePassword(login.password, user.password)) {
            logger.info("User $user.id authenticated with successfully")

            return generateAuthenticationTokens(user, device).also {
                emailService.sendNewAccessEmail(user, device)
            }
        }
        throw BadCredentialsException()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun authenticationValidate(
        userId: String,
        sessionId: String,
        refreshToken: String
    ): Authentication? {
        logger.info("Attempting to validate authentication for session $sessionId of user $userId")

        val session =
            sessionService.findByIdAndUserId(sessionId, userId)
        val user = userRepository.findById(userId).getOrNull() ?: throw UserNotFoundException()

        return when {
            isExpired() -> {
                if (session.refreshToken == refreshToken) {
                    return generateAuthenticationTokens(
                        user,
                        session.device,
                        sessionId
                    )
                }
                throw SessionException("Refresh token not matches")
            }

            else -> {
                logger.info("Session $sessionId for user $userId is authenticate ,valid and not revoked")
                null
            }
        }
    }

    fun generateAuthenticationTokens(user: User, device: Device, sessionId: String? = null): Authentication {
        val refreshToken = generateRandomString()
        val session =
            sessionService.save(sessionId = sessionId, userId = user.id, device = device, refreshToken = refreshToken)
        val token = JWTUtils.generateToken(user.id, secret, listOf(ROLE_USER), session.id, expirationAuthToken.toLong())

        logger.info("Authentication successfully generated for session ${session.id} of user ${user.id}")

        return Authentication(token, refreshToken, user)
    }

    fun logout(userId: String, sessionId: String) {
        sessionService.revoke(sessionId, userId)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}