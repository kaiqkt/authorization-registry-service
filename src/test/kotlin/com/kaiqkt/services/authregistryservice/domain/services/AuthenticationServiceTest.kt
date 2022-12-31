package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.LoginSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionException
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
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class AuthenticationServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val sessionService: SessionService = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val customAccessTokenSecret: String = "shared-secret"
    private val customAccessTokenExpiration: String = "43200000"
    private val authenticationService: AuthenticationService =
        AuthenticationService(userRepository, sessionService, emailService, customAccessTokenSecret, customAccessTokenExpiration)

    @Test
    fun `given login, when user exists and password matches, should return the authentication`() {
        val login = LoginSampler.sample()
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findByEmail(any()) } returns user
        every { sessionService.save(sessionId = any(), any(), any(), any()) } returns session
        every { emailService.sendNewAccessEmail(any(), any()) } just runs

        authenticationService.authenticate(device, login)

        verify { userRepository.findByEmail(login.email) }
        verify { emailService.sendNewAccessEmail(user, device) }
        verify { sessionService.save(null, user.id, device, any()) }
    }

    @Test
    fun `given login, when user not exists, should throw UserNotFoundException`() {
        val login = LoginSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findByEmail(any()) } returns null

        assertThrows<UserNotFoundException> {
            authenticationService.authenticate(device, login)
        }

        verify { userRepository.findByEmail(login.email) }
    }

    @Test
    fun `given login, when user password not matches, should throw BadCredentialsException`() {
        val login = LoginSampler.invalidPasswordSample()
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findByEmail(any()) } returns user

        assertThrows<BadCredentialsException> {
            authenticationService.authenticate(device, login)
        }

        verify { userRepository.findByEmail(login.email) }
    }

    @Test
    fun `given a user, user-agent and the app version, should persist the session and return the authentication`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { sessionService.save(any(), any(), any(), any()) } returns session

        val authentication = authenticationService.generateAuthenticationTokens(user, device)

        Assertions.assertEquals(user, authentication.user)
    }

    @Test
    fun `given a generate authentication, when fail to create new session, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { sessionService.save(any(), any(), any(), any()) } throws PersistenceException("Unable to persist")

        assertThrows<PersistenceException> {
            authenticationService.generateAuthenticationTokens(user, device)
        }

        verify { sessionService.save(any(), any(), any(), any()) }
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
    fun `given a session to validate, when the access token is not expired and the session is not revoked, should return null`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.findByIdAndUserId(any(), any()) } returns session

        val response = authenticationService.authenticationValidate(user.id, session.id, refreshToken)

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify { userRepository.findById(user.id) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given a session to validate, when the access token is expired, the session is not revoked and the refresh token matches, should refresh the authentication tokens`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sampleExpired()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.findByIdAndUserId(any(), any()) } returns session

        val response = authenticationService.authenticationValidate(user.id, session.id, refreshToken)

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify { sessionService.save(any(), any(), any(), any()) }
        verify { userRepository.findById(user.id) }

        Assertions.assertNotNull(response)
    }

    @Test
    fun `given a session to validate, when not found the user, should throw UserNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sampleExpired()

        every { userRepository.findById(any()) } returns Optional.empty()
        every { sessionService.findByIdAndUserId(any(), any()) } returns session

        assertThrows<UserNotFoundException> {
            authenticationService.authenticationValidate(user.id, session.id, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify(exactly = 0) { sessionService.save(any(), any(), any(), any()) }
        verify { userRepository.findById(user.id) }
    }

    @Test
    fun `given a session to validate, when the session revoked, should throw SessionNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKAy"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { sessionService.findByIdAndUserId(any(), any()) } throws SessionNotFoundException(session.id, user.id)

        assertThrows<SessionNotFoundException> {
            authenticationService.authenticationValidate(user.id, session.id, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
    }

    @Test
    fun `given a session to validate, when the refresh revoked not match, should throw SessionException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val refreshToken = "031231amdsfakKKA"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sampleExpired()

        every { sessionService.findByIdAndUserId(any(), any()) } returns session
        every { userRepository.findById(any()) } returns Optional.of(user)

        assertThrows<SessionException> {
            authenticationService.authenticationValidate(user.id, session.id, refreshToken)
        }

        verify { sessionService.findByIdAndUserId(session.id, user.id) }
        verify { userRepository.findById(any()) }
    }
}