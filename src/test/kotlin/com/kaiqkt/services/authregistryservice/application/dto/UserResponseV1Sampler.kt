package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.GenreV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserResponseV1

object UserResponseV1Sampler {
    fun sample() = UserResponseV1(
        id = "01GFPPTXKZ8ZJRG8MF701M0W99",
        fullName = "Shinji ikari",
        email = "shinji@eva01.com",
        phone = PhoneV1Sampler.sample(),
        addresses = mutableListOf(AddressV1Sampler.sample()),
        birthDate = "13/11/2001",
        genre = GenreV1.MEN
    )
}