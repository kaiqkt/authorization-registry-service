package com.kaiqkt.services.authregistryservice.domain.entities

data class Address(
    val id: String,
    val zipCode: String,
    val street: String,
    val district: String,
    val complement: String? = null,
    val number: String,
    val city: String,
    val state: String,
    val country: String
)
