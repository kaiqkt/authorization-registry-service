package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.UpdateAddressV1

object UpdateAddressV1Sampler {
    fun sample() = UpdateAddressV1(
        zipCode = "09111611",
        street = "Rua spb",
        number = "30",
        city = "Santo Andre",
        state = "BH",
        country = "VA",
        district = "Vila Florida",
        complement = "Ap 77"
    )
}