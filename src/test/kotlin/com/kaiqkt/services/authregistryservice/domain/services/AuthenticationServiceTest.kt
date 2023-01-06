package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadRefreshTokenException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class AuthenticationServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val sessionService: SessionService = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val customAccessTokenSecret: String = "shared-secret"
    private val customAccessTokenExpiration: String = "2"
    private val authenticationService: AuthenticationService =
        AuthenticationService(userRepository, sessionService, emailService, customAccessTokenSecret, customAccessTokenExpiration)

    @Test
    fun `given login, when user exists and password matches, should return the authentication`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()
        val password = "1234657"

        every { userRepository.findByEmail(any()) } returns user
        every { sessionService.save(any(), any(), any()) } returns session
        every { emailService.sendNewAccessEmail(any(), any()) } just runs

        authenticationService.authenticateWithCredentials(device, user.email, password)

        verify { userRepository.findByEmail(user.email) }
        verify { emailService.sendNewAccessEmail(user, device) }
        verify { sessionService.save(user.id, device, any()) }
    }

    @Test
    fun `given login, when user not exists, should throw UserNotFoundException`() {
        val password = "1234657"
        val device = DeviceSampler.sample()
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns null

        assertThrows<UserNotFoundException> {
            authenticationService.authenticateWithCredentials(device, user.email, password)
        }

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given login, when user password not matches, should throw BadCredentialsException`() {
        val password = "1234658"
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findByEmail(any()) } returns user

        assertThrows<BadCredentialsException> {
            authenticationService.authenticateWithCredentials(device, user.email, password)
        }

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given a user, user-agent and the app version, should persist the session and return the authentication`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { sessionService.save(any(), any(), any()) } returns session

        val authentication = authenticationService.authenticate(user, device)

        verify { sessionService.save(user.id, device, any()) }

        Assertions.assertEquals(user, authentication.user)
    }

    @Test
    fun `given a user, user-agent and the app version, should update the session and return the authentication`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { sessionService.update(any(), any(), any()) } returns session

        val authentication = authenticationService.authenticate(user, device, session.id)

        verify { sessionService.update(session.id, user.id, any()) }

        Assertions.assertEquals(user, authentication.user)
    }

    @Test
    fun `given a generate authentication, when fail to create new session, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { sessionService.save(any(), any(), any()) } throws PersistenceException("Unable to persist")

        assertThrows<PersistenceException> {
            authenticationService.authenticate(user, device)
        }

        verify { sessionService.save(any(), any(), any()) }
    }

    @Test
    fun `given a userId and session id, should logout the session`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        every { sessionService.revoke(any(), any()) } just runs

        authenticationService.logout(user.id, session.id)

        verify { sessionService.revoke(session.id, user.id) }
    }

    @Test
    fun `given a userId and session id, when fail to revoke the session, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        every { sessionService.revoke(any(), any()) } throws PersistenceException("Unable to delete session")

        assertThrows<PersistenceException> {
            authenticationService.logout(user.id, session.id)
        }

        verify { sessionService.revoke(session.id, user.id) }
    }

    @Test
    fun `given a userId and session id, should revoke all session except the current successfully`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        every { sessionService.revokeAllExceptCurrent(any(), any()) } just runs

        authenticationService.logoutAllExceptCurrent(user.id, session.id)

        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
    }

    @Test
    fun `given a userId and session id, when fail to revoke the sessions, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        every { sessionService.revokeAllExceptCurrent(any(), any()) } throws PersistenceException("Unable to delete session")

        assertThrows<PersistenceException> {
            authenticationService.logoutAllExceptCurrent(user.id, session.id)
        }

        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
    }

    @Test
    fun `given a authentication to refresh, when the refresh token is not expired or revoked, should return authentication`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"
        val accessToken = JWTUtils.generateToken(user.id, customAccessTokenSecret, listOf(ROLE_USER), session.id, customAccessTokenExpiration.toLong())

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.findByIdAndUserId(any(), any()) } returns session
        every { sessionService.update(any(), any(), any()) } returns session

        authenticationService.refresh(accessToken, refreshToken)

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify { userRepository.findById(user.id) }
        verify { sessionService.update(session.id, user.id, any()) }
    }

    @Test
    fun `given a authentication to refresh, when not found the user, should throw UserNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"
        val accessToken = JWTUtils.generateToken(user.id, customAccessTokenSecret, listOf(ROLE_USER), session.id, customAccessTokenExpiration.toLong())

        every { userRepository.findById(any()) } returns Optional.empty()
        every { sessionService.findByIdAndUserId(any(), any()) } returns session
        every { sessionService.update(any(), any(), any()) } returns session

        assertThrows<UserNotFoundException> {
            authenticationService.refresh(accessToken, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify(exactly = 0) { sessionService.update(session.id, user.id, any()) }
        verify { userRepository.findById(user.id) }
    }

    @Test
    fun `given a authentication to refresh, when the session revoked, should throw SessionNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"
        val accessToken = JWTUtils.generateToken(user.id, customAccessTokenSecret, listOf(ROLE_USER), session.id, customAccessTokenExpiration.toLong())

        every { sessionService.findByIdAndUserId(any(), any()) } throws SessionNotFoundException()

        assertThrows<SessionNotFoundException> {
            authenticationService.refresh(accessToken, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify(exactly = 0) { sessionService.update(session.id, user.id, any()) }
        verify(exactly = 0) { userRepository.findById(user.id) }
    }

    @Test
    fun `given a authentication to refresh, when the refresh revoked or not match, should throw SessionException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKA"
        val accessToken = JWTUtils.generateToken(user.id, customAccessTokenSecret, listOf(ROLE_USER), session.id, customAccessTokenExpiration.toLong())

        every { sessionService.findByIdAndUserId(any(), any()) } returns session
        every { userRepository.findById(any()) } returns Optional.of(user)

        assertThrows<BadRefreshTokenException> {
            authenticationService.refresh(accessToken, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify { userRepository.findById(any()) }
        verify(exactly = 0) { sessionService.update(session.id, user.id, any()) }
    }
}