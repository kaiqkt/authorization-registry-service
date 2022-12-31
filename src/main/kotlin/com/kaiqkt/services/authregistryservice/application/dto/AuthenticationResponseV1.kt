package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1

fun Authentication.toV1() = AuthenticationResponseV1(
    accessToken = this.accessToken,
    refreshToken = this.refreshToken,
    user = this.user.toV1(),
)