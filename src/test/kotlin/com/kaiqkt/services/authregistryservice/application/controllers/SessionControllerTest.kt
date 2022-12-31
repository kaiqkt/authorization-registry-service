package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.services.SessionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

class SessionControllerTest {
    private val sessionService: SessionService = mockk(relaxed = true)
    private val controller: SessionController = SessionController(sessionService)

    @Test
    fun `given a userId, should return his sessions and http status 200`() {
        val sessions = listOf(SessionSampler.sample())

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { sessionService.findAllByUserId(any()) } returns sessions

        val response = controller.findAllByUserId()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

        verify { sessionService.findAllByUserId("01GFPPTXKZ8ZJRG8MF701M0W99") }
    }
}