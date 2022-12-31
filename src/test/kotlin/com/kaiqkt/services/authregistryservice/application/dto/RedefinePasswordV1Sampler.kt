package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.RedefinePasswordV1

object RedefinePasswordV1Sampler {
    fun sample(code: String) = RedefinePasswordV1(
        code = code,
        newPassword = "12345Ab*"
    )
}