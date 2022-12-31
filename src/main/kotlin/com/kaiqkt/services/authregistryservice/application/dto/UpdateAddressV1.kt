package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.UpdateAddress
import com.kaiqkt.services.authregistryservice.generated.application.dto.UpdateAddressV1

fun UpdateAddressV1.toDomain() = UpdateAddress(
    zipCode = this.zipCode,
    street = this.street,
    district = this.district,
    complement = this.complement,
    number = this.number,
    city = this.city,
    state = this.state,
    country = this.country
)