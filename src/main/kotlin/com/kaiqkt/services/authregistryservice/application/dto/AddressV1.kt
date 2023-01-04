package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1
import io.azam.ulidj.ULID

fun AddressV1.toDomain(id: String? = null) = Address(
    id = id ?: ULID.random(),
    zipCode = this.zipCode,
    street = this.street,
    district = this.district,
    complement = this.complement,
    number = this.number,
    city = this.city,
    state = this.state,
    country = this.country
)

fun Address.toV1() = AddressV1(
    id = this.id,
    zipCode = this.zipCode,
    street = this.street,
    number = this.number,
    district = this.district,
    city = this.city,
    state = this.state,
    country = this.country,
    complement = this.complement
)