package com.kaiqkt.services.authregistryservice.resources.communication

import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailRequestSampler
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailType
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommunicationServiceImplementationTest {
    private val communicationClient: CommunicationClient = mockk(relaxed = true)
    private val emailTemplatesProvider: EmailTemplateProvider = EmailTemplateProvider(
        "s3://communication-d-1/emails/",
        "password-reset.html",
        "password-updated.html",
        "welcome.html",
        "new-access.html"
    )
    private val communicationService: CommunicationService = CommunicationServiceImplementation(
        communicationClient,
        emailTemplatesProvider,
    )

    @Test
    fun `given the request to send an email, when is a password reset email, should be sent with the correct subject and url template`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji",
            "code" to "1234"
        )
        val emailRequest = EmailRequestSampler.passwordResetEmailSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(recipient, templateData, EmailType.PASSWORD_RESET_REQUEST)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a password new access email, should be sent with the correct subject and url template`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji"
        )
        val emailRequest = EmailRequestSampler.newAccessSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(recipient, templateData, EmailType.NEW_ACCESS)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a welcome email, should must be sent with the correct subject and url template`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji"
        )
        val emailRequest = EmailRequestSampler.welcomeEmailSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(recipient, templateData, EmailType.WELCOME_TEMPLATE)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a password updated email, should must be sent with the correct subject and url template`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji"
        )
        val emailRequest = EmailRequestSampler.passwordUpdatedSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(recipient, templateData, EmailType.PASSWORD_UPDATED)

        verify { communicationClient.sendEmail(emailRequest) }
    }


    @Test
    fun `given the request to send an email, when fail to send, should throw ResourceException`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji",
            "code" to "1234"
        )

        every { communicationClient.sendEmail(any()) } throws ResourceException(
            "Fail to send email kadkiasd@gmail.com, status 500, result: null"
        )

        assertThrows<ResourceException> {
            communicationService.sendEmail(recipient, templateData, EmailType.PASSWORD_RESET_REQUEST)
        }
    }
}