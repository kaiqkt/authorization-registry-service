package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserValidatorTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val validator: UserValidator = UserValidator(userRepository)

    @Test
    fun `given a valid user, should not return error message`() {
        val user = UserSampler.sample()

        every { userRepository.existsByEmail(any()) } returns false

        val validation = validator.validate(user)

        Assertions.assertNull(validation.errorMessage)

        verify { userRepository.existsByEmail(user.email) }
    }

    @Test
    fun `given a valid user,when email is already used, should return validation error`() {
        val user = UserSampler.sample()

        every { userRepository.existsByEmail(any()) } returns true

        val validation = validator.validate(user)

        Assertions.assertNotNull(validation)

        verify { userRepository.existsByEmail(user.email) }
    }

}