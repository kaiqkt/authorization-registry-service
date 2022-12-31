package com.kaiqkt.services.authregistryservice.domain.exceptions

open class DomainException(
    override val message: String,
    override val cause: Throwable? = null,
): Exception(message, cause)