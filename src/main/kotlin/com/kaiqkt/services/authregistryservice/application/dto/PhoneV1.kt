package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.generated.application.dto.PhoneV1

private const val PHONE_REGEX = "/[^a-zA-Z0-9 ]/g"

fun PhoneV1.toDomain() = Phone(
    countryCode = this.countryCode.replace(PHONE_REGEX.toRegex(), ""),
    areaCode = this.areaCode.replace(PHONE_REGEX.toRegex(), ""),
    number = this.number.replace(PHONE_REGEX.toRegex(), "")
)

fun Phone.toV1() = PhoneV1(
    countryCode = this.countryCode,
    areaCode = this.areaCode,
    number = this.number
)