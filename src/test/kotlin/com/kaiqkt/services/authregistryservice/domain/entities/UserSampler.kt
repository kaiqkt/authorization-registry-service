package com.kaiqkt.services.authregistryservice.domain.entities

object UserSampler {
    fun sample() = User(
        id = "01GFPPTXKZ8ZJRG8MF701M0W99",
        fullName = "Shinji ikari",
        email = "shinji@eva01.com",
        phone = PhoneSampler.sample(),
        birthDate = "13/11/2001",
        addresses = mutableListOf(AddressSampler.sample()),
        genre = Genre.MEN,
        password = PasswordSampler.sample()
    )
}