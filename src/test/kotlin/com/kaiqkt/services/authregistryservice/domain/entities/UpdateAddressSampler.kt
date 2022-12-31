package com.kaiqkt.services.authregistryservice.domain.entities

object UpdateAddressSampler {
    fun sample() = UpdateAddress(
        zipCode = "09111611",
        street = "Rua bps",
        number = "30",
        city = "Santo Andre",
        state = "BH",
        country = "VA",
        district = "Vila Florida",
        complement = "Ap 77"
    )

    fun sampleUpdateAddressNull() = UpdateAddress()
}