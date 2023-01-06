package com.kaiqkt.services.authregistryservice.domain.exceptions

class BadRefreshTokenException :
    DomainException(ErrorType.REFRESH_TOKEN_INCORRECT, "Incorrect refresh token")