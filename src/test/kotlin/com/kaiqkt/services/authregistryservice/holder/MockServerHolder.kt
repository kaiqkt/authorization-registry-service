package com.kaiqkt.services.authregistryservice.holder

import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.verify.VerificationTimes

abstract class MockServerHolder {

    private val port: Int = 8081

    private val mockServer: ClientAndServer = ClientAndServer.startClientAndServer(port)
    private val baseUrl = "http://127.0.0.1:${mockServer.localPort}"

    fun baseUrl() = baseUrl

    fun mockServer(): ClientAndServer = mockServer

    fun reset(): MockServerClient = mockServer.clear(HttpRequest.request().withPath("/.*"))

    fun verifyRequest(httpRequest: HttpRequest, count: Int = 1) {
        mockServer.verify(
            httpRequest,
            VerificationTimes.exactly(count)
        )
    }
}