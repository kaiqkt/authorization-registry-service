package com.kaiqkt.services.authregistryservice.application.handler

import com.kaiqkt.services.authregistryservice.domain.exceptions.AddressNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.DomainException
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadRefreshTokenException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.InvalidFieldErrorV1
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException, request: WebRequest): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Bad credentials exception error: ${ex.message}")

        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException, request: WebRequest): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("User not found exception error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException, request: WebRequest): ResponseEntity<InvalidFieldErrorV1> {
        val response = InvalidFieldErrorV1(ex.errors)

        logger.error("Validation exception error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadRefreshTokenException::class)
    fun handleBadRefreshTokenException(ex: BadRefreshTokenException, request: WebRequest): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Bad refresh token error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(SessionNotFoundException::class)
    fun handleSessionNotFoundException(
        ex: SessionNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Session not found exception error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(InvalidRedefinePasswordException::class)
    fun handleInvalidRedefinePasswordException(
        ex: InvalidRedefinePasswordException,
        request: WebRequest
    ): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Redefine password error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AddressNotFoundException::class)
    fun handleAddressNotFoundException(
        ex: AddressNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorV1> {
        val response = ErrorV1(ex.type.name, ex.message)

        logger.error("Address not found exception error: ${getUri(request)}")

        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    public override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors: MutableMap<String, Any?> = HashMap()
        val responseBody = InvalidFieldErrorV1(errors)

        ex.bindingResult.allErrors.forEach { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage
        }
        logger.error("Method argument not valid exception error: ${getUri(request)}")

        return ResponseEntity(responseBody, HttpStatus.BAD_REQUEST)
    }

    private fun getUri(request: WebRequest): List<String> = request.getDescription(true).split(";")

}