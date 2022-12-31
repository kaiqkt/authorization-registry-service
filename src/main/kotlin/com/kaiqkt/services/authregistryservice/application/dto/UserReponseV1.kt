package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.generated.application.dto.GenreV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserResponseV1

fun User.toV1() = UserResponseV1(
    id = this.id,
    fullName = this.fullName,
    email = this.email,
    phone = this.phone.toV1(),
    birthDate = this.birthDate,
    genre = GenreV1.valueOf(this.genre.name.replaceFirstChar { it.lowercaseChar() }),
    addresses = this.addresses.map { it.toV1() }
)