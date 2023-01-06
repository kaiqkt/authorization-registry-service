package com.kaiqkt.services.authregistryservice.domain.exceptions

enum class ErrorType {
    INCORRECT_PASSWORD,
    REFRESH_TOKEN_INCORRECT,
    INVALID_REDEFINE_PASSWORD_CODE,
    INVALID_FIELDS_ERROR,
    ADDRESS_NOT_FOUND,
    SESSION_NOT_FOUND,
    USER_NOT_FOUND
}