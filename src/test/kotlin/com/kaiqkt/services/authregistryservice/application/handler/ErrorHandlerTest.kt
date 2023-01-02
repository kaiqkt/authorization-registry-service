package com.kaiqkt.services.authregistryservice.application.handler

import com.kaiqkt.services.authregistryservice.application.dto.ErrorSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AddressNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.DomainException
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadRefreshTokenException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import io.azam.ulidj.ULID
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.MapBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest


class ErrorHandlerTest {

    private val webRequest: WebRequest = mockk(relaxed = true)

    @Test
    fun `given an DomainException, when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sample()
        val exception = DomainException(error.details.toString())

        val response = errorHandler.handleDomainException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an BadCredentialsException when handling, should return HTTP status 403`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleBadCredentialsError()
        val exception = BadCredentialsException()

        val response = errorHandler.handleBadCredentialsException(exception)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an UserNotFoundException when handling, should return HTTP status 404`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleUserNotFoundError()
        val exception = UserNotFoundException()

        val response = errorHandler.handleUserNotFoundException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an ValidationException when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleValidationError()
        val exception = ValidationException(mapOf("email" to "must match \"\\S+@\\S+\\.\\S+\""))

        val response = errorHandler.handleValidationException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an SessionException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleSessionError()
        val exception = BadRefreshTokenException("Session revoked")

        val response = errorHandler.handleBadRefreshTokenException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an SessionNotFoundException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val sessionId = ULID.random()
        val userId = ULID.random()
        val error = ErrorSampler.sampleSessionNotFoundError(sessionId, userId)
        val exception = SessionNotFoundException(sessionId, userId)

        val response = errorHandler.handleSessionNotFoundException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an ResetPasswordCodeNotFoundException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleResetPasswordCodeNotFoundException()
        val exception = InvalidRedefinePasswordException()

        val response = errorHandler.handleInvalidRedefinePasswordException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an AddressNotFoundException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleAddressNotFoundException()
        val exception = AddressNotFoundException()

        val response = errorHandler.handleAddressNotFoundException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(error.details, response.body?.details)
    }

    @Test
    fun `given an MethodArgumentNotValidException when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleMethodArgumentNotValidError()

        val parameter: MethodParameter = mockk(relaxed = true)

        val headers = HttpHeaders()
        headers.add("test", "test")

        val bindingResult: BindingResult = MapBindingResult(mapOf<String, String>(), "objectName")
        bindingResult.addError(FieldError("objectName", "field1", "message"))
        bindingResult.addError(FieldError("objectName", "field2", "message"))


        val exception = MethodArgumentNotValidException(parameter, bindingResult)

        val response = errorHandler.handleMethodArgumentNotValid(
            exception,
            headers,
            HttpStatus.OK,
            webRequest
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error, response.body)
    }

}