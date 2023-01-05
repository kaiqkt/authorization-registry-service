package com.kaiqkt.services.authregistryservice.application.controllers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.application.dto.AddressV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.ErrorSampler
import com.kaiqkt.services.authregistryservice.application.dto.NewPasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.PhoneV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordRequestV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.UserV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.Genre
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserResponseV1
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import io.azam.ulidj.ULID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import kotlin.jvm.optionals.getOrNull

class UserTest : ApplicationIntegrationTest() {

    @Test
    fun `giving a request to create a new user, when it is valid, should save it in mongo, send email request and return http status 201 with authentication response`() {
        val request = UserV1Sampler.sample()

        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .post()
            .uri("/user")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(AuthenticationResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                val token = JWTUtils.getClaims(body?.accessToken!!, customerSecret)
                Assertions.assertNotNull(body)

                val session = sessionRepository.findByIdAndUserId(token.sessionId, body.user.id)

                Assertions.assertNotNull(session)
            }

        val user = userRepository.findAll()[0]

        val passwordDecrypted = EncryptUtils.validatePassword(request.password, user.password)
        val expectedAddress = request.address?.toDomain()?.copy(id = user.addresses[0].id)

        Assertions.assertNotNull(user)
        Assertions.assertEquals(request.fullName, user.fullName)
        Assertions.assertEquals(request.email, user.email)
        Assertions.assertTrue(passwordDecrypted)
        Assertions.assertEquals(request.phone.toDomain(), user.phone)
        Assertions.assertEquals(request.birthDate, user.birthDate)
        Assertions.assertEquals(Genre.valueOf(request.genre.value), user.genre)
        Assertions.assertEquals(mutableListOf(expectedAddress), user.addresses)

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given an access token, when exist, should return user response and status http 200`() {
        val user = UserSampler.sample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        webTestClient
            .get()
            .uri("/user")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UserResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(body?.fullName, user.fullName)
                Assertions.assertEquals(body?.email, user.email)
                Assertions.assertEquals(body?.phone?.toDomain(), user.phone)
                Assertions.assertEquals(body?.birthDate, user.birthDate)
                Assertions.assertEquals(Genre.valueOf(body?.genre!!.value), user.genre)
            }

        Assertions.assertEquals(userRepository.findAll().size, 1)
    }

    @Test
    fun `given an access token, when not exist, should return http 404`() {
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        webTestClient
            .get()
            .uri("/user")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `giving a request to create a new user, when some field is invalid, should return http status 400`() {

        val request = UserV1Sampler.sampleWithInvalidEmail()
        val expectedError = ErrorSampler.sampleValidationError()

        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .post()
            .uri("/user")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ErrorResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(expectedError.details, body?.details)
            }

        Assertions.assertEquals(userRepository.findAll().size, 0)
    }

    @Test
    fun `given a get user request, when the user not exist, should return http status 404`() {

        webTestClient
            .get()
            .uri("/user/${ULID.random()}")
            .header(Headers.AUTHORIZATION, serviceSecret)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `given a get user request, when is user exists in, should return http status 200 with user response`() {
        val user = UserSampler.sample()
        userRepository.save(user)
        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .get()
            .uri("/user/${user.id}")
            .header(Headers.AUTHORIZATION, serviceSecret)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UserResponseV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(body?.fullName, user.fullName)
                Assertions.assertEquals(body?.email, user.email)
                Assertions.assertEquals(body?.phone?.toDomain(), user.phone)
                Assertions.assertEquals(body?.birthDate, user.birthDate)
                Assertions.assertEquals(Genre.valueOf(body?.genre!!.value), user.genre)
            }

        Assertions.assertEquals(userRepository.findAll().size, 1)
    }

    @Test
    fun `given a request to update password, when the actual password matches, should update, delete all sessions except the current and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.sample()
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session1.id, sessionExpiration.toLong())

        sessionRepository.save(session1)
        sessionRepository.save(session2)

        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isNoContent

        Assertions.assertEquals(sessionRepository.findAllUserId(user.id).size, 1)

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to update password, when the actual password not matches, should throw http status 401`() {
        val user = UserSampler.sample()
        val session1 = SessionSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.invalidPasswordSample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session1.id, sessionExpiration.toLong())

        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isUnauthorized

    }

    @Test
    fun `given a request to update password, when the new password not match with the requirements, should throw http status 400 and return a message`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.invalidNewPasswordSample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session.id, sessionExpiration.toLong())


        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `given a request to generate redefine password code, when the user exist, should create de code and send the email and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val request = RedefinePasswordRequestV1Sampler.sample()

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/user/redefine-password")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to redefine the password, when the code exist, should update the password and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val redefinePasswordCode = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(redefinePasswordCode)
        val session = SessionSampler.sample()

        userRepository.save(user)
        sessionRepository.save(session)
        redefinePasswordRepository.save(redefinePasswordCode, user.id)

        webTestClient
            .put()
            .uri("/user/redefine-password")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to redefine the password, when the code not exist, should return http status 401`() {
        val user = UserSampler.sample()
        val code = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(code)

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/user/redefine-password/code")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `given a request to validate the redefine code, when exist and is not expired, should just return http status 204`() {
        val user = UserSampler.sample()
        val redefinePasswordCode = generateRandomString()

        userRepository.save(user)
        redefinePasswordRepository.save(redefinePasswordCode, user.id)

        webTestClient
            .get()
            .uri("/user/redefine-password/$redefinePasswordCode")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `given a request to validate the redefine code, when is expired and not exist anymore, should delete him and return http status 401`() {
        val user = UserSampler.sample()
        val redefinePasswordCode = generateRandomString()

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/user/redefine-password/$redefinePasswordCode")
            .exchange()
            .expectStatus()
            .isUnauthorized

        Assertions.assertNull(redefinePasswordRepository.findByCode(redefinePasswordCode))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to update, when exist, should update successfully and return http status 200`() {
        val request = AddressV1Sampler.sample()

        val user = UserSampler.sample()
        user.addresses.add(AddressSampler.sample().copy(id = ULID.random()))
        val address = AddressSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/user/address/${address.id}")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_address_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!
        val updated = userUpdated.addresses.first { it.id == address.id }
        val notUpdated = userUpdated.addresses.first { it.id != address.id }

        Assertions.assertEquals(request.street, updated.street)
        Assertions.assertEquals("Rua spb", notUpdated.street)
    }

    @Test
    fun `given a address to update, when not exist, should return http status 404`() {
        val address = AddressV1Sampler.sample()
        val user = UserSampler.sample()
        user.addresses.add(AddressSampler.sample().copy(id = ULID.random()))
        val addressId = ULID.random()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/user/address/$addressId")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_address_v1+json"))
            .bodyValue(address)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to delete, when exist, should update successfully and return http status 204`() {
        val user = UserSampler.sample()
        user.addresses.add(AddressSampler.sample().copy(id = ULID.random()))
        val address = AddressV1Sampler.sample()

        val token = JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), ULID.random(), sessionExpiration.toLong())

        userRepository.save(user)

        webTestClient
            .delete()
            .uri("/user/address/${address.id}")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!

        Assertions.assertEquals(1, userUpdated.addresses.size)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a address to create, when user exist, should create successfully and return http status 204`() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())
        val address = AddressV1Sampler.sample()

        val token = JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), ULID.random(), sessionExpiration.toLong())

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/user/address")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_address_v1+json"))
            .bodyValue(address)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()!!

        Assertions.assertEquals(1, userUpdated.addresses.size)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a phone to update, when user exist, should update successfully and return http status 204`() {
        val user = UserSampler.sample().copy(addresses = mutableListOf())
        val phoneUpdate = PhoneV1Sampler.sample().copy(number = "0800-9000")

        val token = JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), ULID.random(), sessionExpiration.toLong())

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/user/phone")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_phone_v1+json"))
            .bodyValue(phoneUpdate)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()

        Assertions.assertEquals(phoneUpdate.number, userUpdated?.phone?.number)
    }
}