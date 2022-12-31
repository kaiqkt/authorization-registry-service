package com.kaiqkt.services.authregistryservice.domain.validation

data class Validation<T>(
    val fieldName: String,
    val fieldValue: T,
    var errorMessage: String? = null
)
