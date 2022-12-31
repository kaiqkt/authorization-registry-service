package com.kaiqkt.services.authregistryservice.domain.entities

data class UpdateAddress(
    var zipCode: String? = null,
    val street: String? = null,
    val district: String? = null,
    val complement: String? = null,
    val number: String? = null,
    val city: String? = null,
    val state: String? = null,
    var country: String? = null
)
