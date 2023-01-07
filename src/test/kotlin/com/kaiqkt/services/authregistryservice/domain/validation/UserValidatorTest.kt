package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.PhoneSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserValidatorTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val userValidator: UserValidator = UserValidator(userRepository)

    @Test
    fun `given a valid user, should not return error message`() {
        val user = UserSampler.sample()

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.existsByPhone(any()) } returns false

        val validation = userValidator.validate(user)

        Assertions.assertTrue(validation.isEmpty())

        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.existsByPhone(user.phone) }
    }

    @Test
    fun `given a user,when email is already used, should return validation error`() {
        val user = UserSampler.sample()

        every { userRepository.existsByEmail(any()) } returns true
        every { userRepository.existsByPhone(any()) } returns false

        val validation = userValidator.validate(user)

        Assertions.assertTrue(validation.isNotEmpty())

        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.existsByPhone(user.phone) }
    }

    @Test
    fun `given a user,when phone is already used, should return validation error`() {
        val user = UserSampler.sample()
        val expectedError = mutableMapOf("phone" to listOf(ValidationType.PHONE_IN_USE))


        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.existsByPhone(any()) } returns true

        val validation = userValidator.validate(user)

        Assertions.assertTrue(validation.isNotEmpty())

        Assertions.assertEquals(expectedError, validation)

        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.existsByPhone(user.phone) }
    }

    @Test
    fun `given a user,when phone invalid, should return validation error`() {
        val phone = PhoneSampler.sampleWithInvalidNumber()
        val expectedError = mutableMapOf("phone" to listOf(ValidationType.INVALID_PHONE))

        every { userRepository.existsByPhone(any()) } returns false

        val validation = userValidator.validate(phone)

        Assertions.assertTrue(validation.isNotEmpty())

        Assertions.assertEquals(expectedError, validation)

        verify { userRepository.existsByPhone(phone) }
    }
}