package com.kaiqkt.services.authregistryservice.domain.exceptions

open class DomainException(
    val type: ErrorType,
    override val message: String
) : Exception(message)