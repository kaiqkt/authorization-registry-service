package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.NewPasswordV1

object NewPasswordV1Sampler {
    fun sample() = NewPasswordV1(
        actualPassword = "1234657",
        newPassword = "Aa#34578"
    )

    fun invalidPasswordSample() = NewPasswordV1(
        actualPassword = "12346578",
        newPassword = "Aa#34578"
    )

    fun invalidNewPasswordSample() = NewPasswordV1(
        actualPassword = "12346578",
        newPassword = "12346578"
    )
}