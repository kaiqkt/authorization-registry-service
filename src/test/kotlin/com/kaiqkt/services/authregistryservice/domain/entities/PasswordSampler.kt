package com.kaiqkt.services.authregistryservice.domain.entities

import com.kaiqkt.commons.crypto.encrypt.Password

object PasswordSampler {
    fun sample() = Password(
        hash = "c97a7c639ed612f64540c887cf57e82c5aceccb92a347c2dc18bb740a61b09a7",
        salt = "61d64f57b5638b241c055f2856153cf8"
    )
}