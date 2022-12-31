package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorResponseV1

object ErrorSampler {
    fun sample() = ErrorResponseV1(
        details = "test"
    )

    fun sampleBadCredentialsError() = ErrorResponseV1(
        details = "Password does not match"
    )

    fun sampleUserNotFoundError() = ErrorResponseV1(
        details = "User not found"
    )

    fun sampleValidationError() = ErrorResponseV1(
        details = mapOf("email" to "must match \"\\S+@\\S+\\.\\S+\"")
    )

    fun sampleSessionError() = ErrorResponseV1(
        details = "Session revoked"
    )

    fun sampleSessionNotFoundError(sessionId: String, userId: String) = ErrorResponseV1(
        details = "Session $sessionId for user $userId not found"
    )

    fun sampleResetPasswordCodeNotFoundException() = ErrorResponseV1(
        details = "Reset code not exist or is expired"
    )

    fun sampleAddressNotFoundException() = ErrorResponseV1(
        details = "Address not found"
    )

    fun sampleMethodArgumentNotValidError() = ErrorResponseV1(
        details = mapOf("field1" to "message", "field2" to "message")
    )
}