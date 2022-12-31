package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1

object AuthenticationResponseSampler {
    fun sample() = AuthenticationResponseV1(
        accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwMUdGUFBUWEtaOFpKUkc4TUY3MDFNMFc5OSIsImlzcyI6IkthaXFrdCIsInNlc3Npb25faWQiOiIwMUdGUFBUWEtaOFpKUkc4TUY3MDFNMFc4OCIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJleHAiOjE2NzA1MTg0MzV9.TdpdvXSKCtehQZma1k4CKCtSGDWIhRQRmkAPba134euEvhYKgZzR16zkmSI7pkGiH3G6HkuGtXp5CKSCDddgKA",
        refreshToken = "031231amdsfakKKAy",
        user = UserResponseV1Sampler.sample()
    )
}