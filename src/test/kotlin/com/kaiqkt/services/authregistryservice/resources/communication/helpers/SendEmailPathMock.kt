package com.kaiqkt.services.authregistryservice.resources.communication.helpers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.services.authregistryservice.holder.MockServerHolder
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

class SendEmailPathMock(private val holder: MockServerHolder) {
    private val mockServer = holder.mockServer()

    fun mockSendEmail() {
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Headers.CONTENT_TYPE, "application/vnd.kaiqkt_email_v1+json")
                .withPath("/email")
        ).respond(
            HttpResponse.response()
                .withStatusCode(204)
        )
    }

    fun mockSendEmailError() {
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("POST")
                .withHeader(Headers.CONTENT_TYPE, "application/vnd.kaiqkt_email_v1+json")
                .withPath("/email")
        ).respond(
            HttpResponse.response()
                .withStatusCode(400)
        )
    }

    fun verifySendEmail(count: Int) = verify(count)

    private fun verify(count: Int) {
        val httpRequest = HttpRequest.request()
            .withMethod("POST")
            .withPath("/email")
        holder.verifyRequest(httpRequest, count)
    }
}
