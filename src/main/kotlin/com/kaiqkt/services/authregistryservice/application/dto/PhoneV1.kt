package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.generated.application.dto.PhoneV1

fun PhoneV1.toDomain() = Phone(
    countryCode = this.countryCode,
    areaCode = this.areaCode,
    number = this.number
)

fun Phone.toV1() = PhoneV1(
    countryCode = this.countryCode,
    areaCode = this.areaCode,
    number = this.number
)