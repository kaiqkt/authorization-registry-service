package com.kaiqkt.services.authregistryservice.domain.entities

object SessionSampler {
    fun sample() = Session(
        id = "01GFPPTXKZ8ZJRG8MF701M0W88",
        userId = "01GFPPTXKZ8ZJRG8MF701M0W99",
        device = DeviceSampler.sample(),
        refreshToken = "031231amdsfakKKAy"
    )
}