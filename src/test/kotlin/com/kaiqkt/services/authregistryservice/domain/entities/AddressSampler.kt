package com.kaiqkt.services.authregistryservice.domain.entities

object AddressSampler {
    fun sample() = Address(
        id = "01GKNAXJG7T7QPRC8JT87DVHT8",
        zipCode = "09111611",
        street = "Rua spb",
        number = "30",
        city = "Santo Andre",
        state = "BH",
        country = "VA",
        district = "Vila Florida"
    )

    fun sampleWithInvalidStreet() = Address(
        zipCode = "09111-666111",
        street = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean ma",
        number = "12345678901",
        city = "12345678901123456789011234567890112345678901",
        state = "BHh",
        complement = "123456789011234567890111",
        country = "VAT",
        district = "Vila Florida"
    )
}