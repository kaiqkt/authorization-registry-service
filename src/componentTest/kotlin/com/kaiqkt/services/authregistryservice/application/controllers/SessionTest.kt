package com.kaiqkt.services.authregistryservice.application.controllers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.generated.application.dto.SessionResponseV1
import io.azam.ulidj.ULID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

class SessionTest : ApplicationIntegrationTest() {

    @Test
    fun `giving a request to get all sesions, when exist sessions for this user, should return the sessions with http status 200`() {
        val user = UserSampler.sample().copy(id = ULID.random())
        val session = SessionSampler.sample().copy(id = ULID.random(), userId = user.id)
        val session2 = SessionSampler.sample().copy(id = ULID.random(), userId = user.id)
        val session3 = SessionSampler.sample().copy(id = ULID.random(), userId = user.id)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session.id, sessionExpiration.toLong())

        sessionRepository.save(session)
        sessionRepository.save(session2)
        sessionRepository.save(session3)

        webTestClient
            .get()
            .uri("/session")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(SessionResponseV1::class.java)
            .consumeWith<WebTestClient.ListBodySpec<SessionResponseV1>> {
                val sessions = it.responseBody

                Assertions.assertEquals(3, sessions?.size)
            }
    }
}