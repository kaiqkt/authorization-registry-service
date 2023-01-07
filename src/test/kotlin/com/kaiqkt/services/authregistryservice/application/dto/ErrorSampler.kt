package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.domain.validation.ValidationType
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.InvalidFieldErrorV1

object ErrorSampler {
    fun sample() = ErrorV1(
        type = ErrorType.USER_NOT_FOUND.name,
        message = "test"
    )

    fun sampleBadCredentialsError() = ErrorV1(
        type = ErrorType.INCORRECT_PASSWORD.name,
        message = "Incorrect password"
    )

    fun sampleUserNotFoundError() = ErrorV1(
        type = ErrorType.USER_NOT_FOUND.name,
        message = "User not found"
    )

    fun sampleValidationError() = InvalidFieldErrorV1(
        details = mutableMapOf("email" to ValidationType.EMAIL_IN_USE)
    )

    fun sampleRefreshTokenError() = ErrorV1(
        type = ErrorType.REFRESH_TOKEN_INCORRECT.name,
        message = "Incorrect refresh token"
    )

    fun sampleSessionNotFoundError() = ErrorV1(
        type = ErrorType.SESSION_NOT_FOUND.name,
        message = "Session not found"
    )

    fun sampleRedefinePasswordCodeNotFoundException() = ErrorV1(
        type = ErrorType.INVALID_REDEFINE_PASSWORD_CODE.name,
        message =  "Reset code not exist or is expired"
    )

    fun sampleAddressNotFoundException() = ErrorV1(
        type = ErrorType.ADDRESS_NOT_FOUND.name,
        message = "Address not found"
    )

    fun sampleMethodArgumentNotValidError() = InvalidFieldErrorV1(
        details = mapOf("email" to "must match \"\\S+@\\S+\\.\\S+\"")
    )
}