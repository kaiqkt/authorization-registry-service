package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailType
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class EmailServiceTest {
    private val communicationService: CommunicationService = mockk(relaxed = true)
    private val emailService: EmailService = EmailService(communicationService)

    @Test
    fun `given welcome email, should send with successfully`() {
        val user = UserSampler.sample()
        val templateData = mapOf("name" to user.firstName)

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendWelcomeEmail(user)

        verify { communicationService.sendEmail(user.email, templateData, EmailType.WELCOME_TEMPLATE) }
    }

    @Test
    fun `given welcome email, when fail to send the email, should catch the exception and log the error`() {
        val user = UserSampler.sample()
        val templateData = mapOf("name" to user.firstName)

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        emailService.sendWelcomeEmail(user)

        verify { communicationService.sendEmail(user.email, templateData, EmailType.WELCOME_TEMPLATE) }
    }

    @Test
    fun `given new access email, should send with successfully`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()
        val templateData = mapOf("device" to device.defaultModel)

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendNewAccessEmail(user, device)

        verify { communicationService.sendEmail(user.email, templateData, EmailType.NEW_ACCESS) }
    }

    @Test
    fun `given new access email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendNewAccessEmail(user, device)
        }

        verify { communicationService.sendEmail(user.email, any(), EmailType.NEW_ACCESS) }
    }

    @Test
    fun `given reset password email, should send with successfully`() {
        val user = UserSampler.sample()
        val code = Random.nextInt().toString()
        val templateData = mapOf(
            "name" to user.firstName,
            "code" to code
        )

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendPasswordResetEmail(code, user)

        verify { communicationService.sendEmail(user.email, templateData, EmailType.PASSWORD_RESET_REQUEST) }
    }

    @Test
    fun `given reset password email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()
        val code = Random.nextInt().toString()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendPasswordResetEmail(code, user)
        }

        verify { communicationService.sendEmail(user.email, any(), EmailType.PASSWORD_RESET_REQUEST) }
    }

    @Test
    fun `given password updated email, should send with successfully`() {
        val user = UserSampler.sample()
        val templateData = mapOf("name" to user.firstName)

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendPasswordUpdatedEmail(user)

        verify { communicationService.sendEmail(user.email, templateData, EmailType.PASSWORD_UPDATED) }
    }

    @Test
    fun `given password updated email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendPasswordUpdatedEmail(user)
        }

        verify { communicationService.sendEmail(user.email, any(), EmailType.PASSWORD_UPDATED) }
    }
}