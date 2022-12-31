package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.PhoneV1

object PhoneV1Sampler {
    fun sample() = PhoneV1(
        countryCode = "55",
        areaCode = "11",
        number = "940028922"
    )
}