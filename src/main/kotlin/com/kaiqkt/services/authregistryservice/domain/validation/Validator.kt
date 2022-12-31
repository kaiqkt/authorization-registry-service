package com.kaiqkt.services.authregistryservice.domain.validation

interface Validator {
    fun validate(obj: Any): Validation<*>
}