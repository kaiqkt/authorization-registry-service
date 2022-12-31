package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.services.authregistryservice.domain.entities.Genre
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1

fun UserV1.toDomain() = User(
    fullName = this.fullName,
    email = this.email,
    password = EncryptUtils.encryptPassword(this.password),
    phone = this.phone.toDomain(),
    birthDate = this.birthDate,
    genre = Genre.valueOf(this.genre.value),
    addresses = this.address?.let { mutableListOf(it.toDomain()) } ?: mutableListOf()
)