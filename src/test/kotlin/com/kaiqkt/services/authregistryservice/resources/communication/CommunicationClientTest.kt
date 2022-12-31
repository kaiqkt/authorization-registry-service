package com.kaiqkt.services.authregistryservice.resources.communication

import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailRequestSampler
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommunicationClientTest {
    private val mock = CommunicationServiceMock

    private val communicationClient: CommunicationClient = CommunicationClient(
        CommunicationServiceMock.baseUrl(),
        "secret"
    )

    @BeforeEach
    fun beforeEach() {
        mock.reset()
    }

    @Test
    fun `given the request to send an email, should be sent successfully`() {
        val emailRequest = EmailRequestSampler.welcomeEmailSample()
        mock.sendEmail.mockSendEmail()

        communicationClient.sendEmail(emailRequest)

        mock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given the request to send an email, when communication service return error, should throw ResourceException`() {
        val emailRequest = EmailRequestSampler.welcomeEmailSample()
        mock.sendEmail.mockSendEmailError()

        assertThrows<ResourceException> {
            communicationClient.sendEmail(emailRequest)
        }

        mock.sendEmail.verifySendEmail(1)
    }

}