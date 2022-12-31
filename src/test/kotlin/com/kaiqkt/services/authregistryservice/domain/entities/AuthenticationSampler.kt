package com.kaiqkt.services.authregistryservice.domain.entities

object AuthenticationSampler {
    fun sample() = Authentication(
        accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIwMUdER1o5OUhSUFpTRTZaVDBGTlhBN1daRiIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJleHAiOjk5OTk5OTk5OTk5OX0.Ov7TC7yd0CoSQi0t0cwvXu-Ez8WTkdWf_GuSzliB8nA6pcmIb508weU9eFGSYKZAMfh7ZVBeIqclOoQn3LmYcQ",
        refreshToken = "031231amdsfakKKAy",
        user = UserSampler.sample(),
    )
}