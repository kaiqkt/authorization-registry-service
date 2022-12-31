package com.kaiqkt.services.authregistryservice.domain.entities

data class Authentication(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
