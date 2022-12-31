package com.kaiqkt.services.authregistryservice.domain.entities

object PhoneSampler {
    fun sample() = Phone(
        countryCode = "55",
        areaCode = "11",
        number = "940028922"
    )

    fun sampleWithInvalidNumber() = Phone(
        countryCode = "+1",
        areaCode = "(11)",
        number = "91456-2121"
    )
}