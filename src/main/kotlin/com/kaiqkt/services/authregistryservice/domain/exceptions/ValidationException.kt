package com.kaiqkt.services.authregistryservice.domain.exceptions

class ValidationException(val errors: Map<String, String>) : DomainException(ErrorType.INVALID_FIELDS_ERROR, "Invalid field value")