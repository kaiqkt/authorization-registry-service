package com.kaiqkt.services.authregistryservice.domain.entities

object LoginSampler {
    fun sample() = Login(
        email = "shinji@eva01.com",
        password = "1234657"
    )

    fun invalidPasswordSample() = Login(
        email = "shinji@eva01.com",
        password = "Aa#34579"
    )
}