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
}