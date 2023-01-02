package com.kaiqkt.services.authregistryservice.domain.exceptions

class BadRefreshTokenException(override val message: String) : DomainException(message)