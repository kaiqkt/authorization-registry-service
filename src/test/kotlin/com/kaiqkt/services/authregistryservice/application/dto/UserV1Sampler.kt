package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.GenreV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1

object UserV1Sampler {
    fun sample() = UserV1(
        fullName = "Shinji Ikari",
        email = "shinji@eva01.com",
        password = "Aa#34578",
        phone = PhoneV1Sampler.sample(),
        birthDate = "13/11/2001",
        genre = GenreV1.mEN,
        address = AddressV1Sampler.sample()
    )

    fun sampleWithInvalidEmail() = UserV1(
        fullName = "Shinji Ikari",
        email = "shinjieva01.com",
        password = "Aa#34578",
        phone = PhoneV1Sampler.sample(),
        birthDate = "13/11/2001",
        genre = GenreV1.mEN,
        address = AddressV1Sampler.sample()
    )
}