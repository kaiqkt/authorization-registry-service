package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Login
import com.kaiqkt.services.authregistryservice.generated.application.dto.LoginV1

fun LoginV1.toDomain() = Login(
    email = this.email,
    password = this.password,
)