package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadRefreshTokenException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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

    fun authenticateWithCredentials(device: Device, email: String, password: String): Authentication {
        logger.info("Authenticating user $email")

        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()

        if (EncryptUtils.validatePassword(password, user.password)) {

            return authenticate(user, device).also {
                emailService.sendNewAccessEmail(user, device)
            }
        }
        throw BadCredentialsException()
    }

    fun authenticate(user: User, device: Device? = null, sessionId: String? = null): Authentication {
        val refreshToken = generateRandomString()
        val session = if (sessionId == null) {
            sessionService.save(userId = user.id, device = device!!, refreshToken = refreshToken)
        } else {
            sessionService.update(sessionId, user.id, refreshToken)
        }

        val token = JWTUtils.generateToken(user.id, secret, listOf(ROLE_USER), session.id, expirationAuthToken.toLong())

        logger.info("User ${user.id} authenticated successfully for the session ${session.id}")

        return Authentication(token, refreshToken, user)
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun refresh(accessToken: String, refreshToken: String): Authentication {
        val claims = JWTUtils.getClaims(accessToken, secret)
        val userId = claims.id
        val sessionId = claims.sessionId

        logger.info("Refreshing the authentication for session $sessionId of user $userId")

        val session = sessionService.findByIdAndUserId(sessionId, userId)
        val user = userRepository.findById(userId).getOrNull() ?: throw UserNotFoundException()

        if (session.refreshToken == refreshToken) {
            return authenticate(user = user, sessionId = sessionId)
        }
        throw BadRefreshTokenException("Refresh token is invalid")
    }

    fun logout(userId: String, sessionId: String) {
        sessionService.revoke(sessionId, userId)
    }

    fun logoutAllExceptCurrent(userId: String, sessionId: String) {
        sessionService.revokeAllExceptCurrent(sessionId, userId)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}