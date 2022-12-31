package com.kaiqkt.services.authregistryservice.domain.entities

import io.azam.ulidj.ULID

data class Address(
    val id: String = ULID.random(),
    val zipCode: String,
    val street: String,
    val district: String,
    val complement: String? = null,
    val number: String,
    val city: String,
    val state: String,
    val country: String
)
