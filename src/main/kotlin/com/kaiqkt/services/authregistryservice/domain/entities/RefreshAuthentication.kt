package com.kaiqkt.services.authregistryservice.domain.entities

data class RefreshAuthentication(
    val accessToken: String,
    val refreshToken: String
)