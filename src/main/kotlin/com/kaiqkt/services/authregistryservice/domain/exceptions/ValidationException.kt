package com.kaiqkt.services.authregistryservice.domain.exceptions

import com.kaiqkt.services.authregistryservice.domain.validation.ValidationType

class ValidationException(val errors: MutableMap<String, ValidationType>) :
    DomainException(ErrorType.INVALID_FIELDS_ERROR, "Invalid field value")