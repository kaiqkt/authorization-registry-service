package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1

object AddressV1Sampler {

    fun sample() = AddressV1(
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