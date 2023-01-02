package com.kaiqkt.services.authregistryservice.application.controllers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.application.dto.ErrorSampler
import com.kaiqkt.services.authregistryservice.application.dto.LoginV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorResponseV1
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class AuthenticationTest : ApplicationIntegrationTest() {
    @Test
    fun `given a authentication request, when user exist and password matchs, should return authentication response and http status 200`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val login = LoginV1Sampler.sample()
        val user = UserSampler.sample()

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/auth/login")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_auth_login_v1+json"))
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AuthenticationResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                val token = JWTUtils.getClaims(body?.accessToken!!, customerSecret)

                val session = sessionRepository.findByIdAndUserId(token.sessionId, user.id)

                Assertions.assertEquals(body.refreshToken, session?.refreshToken)
                Assertions.assertEquals(body.user, user.toV1())
            }

        Assertions.assertEquals(userRepository.findAll().size, 1)
        CommunicationServiceMock.sendEmail.mockSendEmailError()
    }

    @Test
    fun `given a authentication request, when password does not match, should throw BadCredentialsException and return http status 401`() {
        val user = UserSampler.sample()
        val login = LoginV1Sampler.sampleInvalidPassword()
        val error = ErrorSampler.sampleBadCredentialsError()

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/auth/login")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_auth_login_v1+json"))
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectBody(ErrorResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(body?.details, error.details)
            }

    }

    @Test
    fun `given a authentication request, when user is not found, should throw UserNotFoundException and return http status 404`() {
        val login = LoginV1Sampler.sample()
        val error = ErrorSampler.sampleUserNotFoundError()

        webTestClient
            .post()
            .uri("/auth/login")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_auth_login_v1+json"))
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ErrorResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(error.details, body?.details)
            }
    }

    @Test
    fun `given a authentication request with invalid email,should throw MethodArgumentNotValidException and return http status 400`() {
        val login = LoginV1Sampler.sampleInvalidEmail()
        val error = ErrorSampler.sampleValidationError()

        webTestClient
            .post()
            .uri("/auth/login")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_auth_login_v1+json"))
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ErrorResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(error.details, body?.details)
            }
    }

    @Test
    fun `given a logout request,should revoke the session for the requested user and return http status 204`() {

        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session.id, sessionExpiration.toLong())

        sessionRepository.save(session)

        webTestClient
            .delete()
            .uri("/auth/logout")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNoContent

        Assertions.assertNull(sessionRepository.findByIdAndUserId(session.id, user.id))
    }

    @Test
    fun `given a logout request for specific session,should revoke the session for the requested user and return http status 204`() {

        val user = UserSampler.sample()
        val session = SessionSampler.sample()

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session.id, sessionExpiration.toLong())

        sessionRepository.save(session)

        webTestClient
            .delete()
            .uri("/auth/logout/${session.id}")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNoContent

        Assertions.assertNull(sessionRepository.findByIdAndUserId(session.id, user.id))
    }

    @Test
    fun `given an authentication to refresh, when the refresh token is not expired and matches, should return http status 200 with new authentication response`() {

        val session = SessionSampler.sample()
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                session.userId,
                customerSecret,
                listOf(ROLE_USER),
                session.id,
                sessionExpiration.toLong()
            )

        userRepository.save(user)
        sessionRepository.save(session)

        webTestClient
            .post()
            .uri("/auth/refresh")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("Refresh-Token", session.refreshToken)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AuthenticationResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody

                val newSession = sessionRepository.findByIdAndUserId(session.id, session.userId)

                Assertions.assertEquals(newSession?.refreshToken, body?.refreshToken)
            }
    }

    @Test
    fun `given an authentication to refresh,given refresh token with not matches, should return http status 401`() {

        val session = SessionSampler.sample()
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                session.userId,
                customerSecret,
                listOf(ROLE_USER),
                session.id,
                -sessionExpiration.toLong()
            )

        userRepository.save(user)
        sessionRepository.save(session)

        webTestClient
            .post()
            .uri("/auth/refresh")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("Refresh-Token", "TEst")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `given an authentication to refresh,when the session is revoked, should return http status 401`() {

        val session = SessionSampler.sample()

        val token =
            JWTUtils.generateToken(
                session.userId,
                customerSecret,
                listOf(ROLE_USER),
                session.id,
                -sessionExpiration.toLong()
            )

        webTestClient
            .post()
            .uri("/auth/refresh")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("Refresh-Token", "TEst")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }
}