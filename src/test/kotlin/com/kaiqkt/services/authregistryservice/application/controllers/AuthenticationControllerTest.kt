package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.commons.security.auth.getSessionId
import com.kaiqkt.services.authregistryservice.application.dto.AuthenticationResponseSampler
import com.kaiqkt.services.authregistryservice.application.dto.LoginV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.AuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.services.AuthenticationService
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

const val USER_AGENT = "Windows NT 10.0; Win64; x64"
const val APP_VERSION = "1.0.0"

class AuthenticationControllerTest {

    private val customAccessTokenSecret: String = "shared-secret"
    private val customAccessTokenExpiration: String = "2"
    private val authenticationService: AuthenticationService = mockk(relaxed = true)
    private val controller: AuthenticationController = AuthenticationController(authenticationService)

    @Test
    fun `given a request to authenticate user, when successfully authenticate, should return AuthenticationResponse and return http status 200`() {
        val login = LoginV1Sampler.sample()
        val authentication = AuthenticationSampler.sample()
        val expectedResponse = AuthenticationResponseSampler.sample()
        val device = DeviceSampler.sample()

        every { authenticationService.authenticateWithCredentials(any(), any()) } returns authentication

        val response = controller.authenticate(USER_AGENT, APP_VERSION, login)

        verify { authenticationService.authenticateWithCredentials(device, login.toDomain()) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body?.accessToken)
        Assertions.assertNotNull(response.body?.refreshToken)
        Assertions.assertEquals(expectedResponse.user, response.body?.user)
    }

    @Test
    fun `given a request to authenticate user, when user don't exist, should throw UserNotFoundException`() {
        val request = LoginV1Sampler.sample()
        val device = DeviceSampler.sample()

        every { authenticationService.authenticateWithCredentials(any(), any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.authenticate(USER_AGENT, APP_VERSION, request)
        }

        verify {
            authenticationService.authenticateWithCredentials(any(), any())
        }

        verify { authenticationService.authenticateWithCredentials(device, request.toDomain()) }
    }

    @Test
    fun `given a request to authenticate user, when the password does not match with the secured password, should throw BadCredentialsException`() {
        val request = LoginV1Sampler.sample()
        val device = DeviceSampler.sample()

        every { authenticationService.authenticateWithCredentials(any(), any()) } throws BadCredentialsException()

        assertThrows<BadCredentialsException> {
            controller.authenticate(USER_AGENT, APP_VERSION, request)
        }

        verify { authenticationService.authenticateWithCredentials(device, request.toDomain()) }
    }

    @Test
    fun `given a request to authenticate user, when fail to create a session, should throw PersistenceException`() {
        val request = LoginV1Sampler.sample()
        val device = DeviceSampler.sample()

        every {
            authenticationService.authenticateWithCredentials(
                any(),
                any()
            )
        } throws PersistenceException("Unable to persist session A231231CBASDK}}")

        assertThrows<PersistenceException> {
            controller.authenticate(USER_AGENT, APP_VERSION, request)
        }

        verify { authenticationService.authenticateWithCredentials(device, request.toDomain()) }
    }

    @Test
    fun `given a request to logout user, when successfully revoke the session, should return http status 204`() {
        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"
        val sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { authenticationService.logout(any(), any()) } just runs

        val response = controller.logout()

        verify { authenticationService.logout(userId, sessionId) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a request to logout user, when fail to persist the revoked session, should throw PersistenceException`() {
        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every {
            authenticationService.logout(
                any(),
                any()
            )
        } throws PersistenceException("Unable to persist session ${getSessionId()}}}")

        assertThrows<PersistenceException> {
            controller.logout()
        }

        verify { authenticationService.logout(any(), any()) }
    }

    @Test
    fun `given a request to logout specific session, when successfully revoke the session, should return http status 204`() {
        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"
        val sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { authenticationService.logout(any(), any()) } just runs

        val response = controller.logoutSessionById(sessionId)

        verify { authenticationService.logout(userId, sessionId) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a request to logout specific session, when fail to persist the revoked session, should throw PersistenceException`() {
        val sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every {
            authenticationService.logout(
                any(),
                any()
            )
        } throws PersistenceException("Unable to persist session ${getSessionId()}}}")

        assertThrows<PersistenceException> {
            controller.logoutSessionById(sessionId)
        }

        verify { authenticationService.logout(any(), any()) }
    }

    @Test
    fun `given a request to authentication refresh, when the refresh token is correctly, should return new authentication and http status 200`() {
        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"
        val sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"
        val refreshToken = "031231amdsfakKKAy"
        val accessToken = JWTUtils.generateToken(userId, customAccessTokenSecret, listOf(ROLE_USER), sessionId, customAccessTokenExpiration.toLong())

        val authentication = AuthenticationSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { authenticationService.refresh(any(), any()) } returns authentication

        val response = controller.refresh(accessToken, refreshToken)

        verify { authenticationService.refresh(accessToken, refreshToken) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(authentication.accessToken, response.body!!.accessToken)
        Assertions.assertEquals(authentication.refreshToken, response.body!!.refreshToken)
    }

//    @Test
//    fun `given a request to authentication refresh, when the access token is expired or is not matches, the session is not revoked but the refresh token not matches or is expired, should throw SessionException`() {
//        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"
//        val sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"
//        val refreshToken = "031231amdsfakKKAy"
//        val accessToken = JWTUtils.generateToken(userId, customAccessTokenSecret, listOf(ROLE_USER), sessionId, customAccessTokenExpiration.toLong())
//
//        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()
//
//        every { authenticationService.refresh(any(), any()) } throws SessionException("Refresh token is invalid")
//
//        assertThrows<SessionException> {
//            controller.refresh(accessToken, refreshToken)
//        }
//
//        verify { authenticationService.refresh(accessToken, refreshToken) }
//    }
}