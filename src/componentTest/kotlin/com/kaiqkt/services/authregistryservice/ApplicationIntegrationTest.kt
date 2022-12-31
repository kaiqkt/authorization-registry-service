package com.kaiqkt.services.authregistryservice

import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
class ApplicationIntegrationTest {

    @Value("\${service-shared-secret}")
    lateinit var serviceSecret: String

    @Value("\${customer-auth-signing-secret}")
    lateinit var customerSecret: String

    @Value("\${session-expiration}")
    lateinit var sessionExpiration: String

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Autowired
    lateinit var redefinePasswordRepository: RedefinePasswordRepository
    @BeforeEach
    fun beforeEach() {
        userRepository.deleteAll()
        CommunicationServiceMock.reset()
    }
}