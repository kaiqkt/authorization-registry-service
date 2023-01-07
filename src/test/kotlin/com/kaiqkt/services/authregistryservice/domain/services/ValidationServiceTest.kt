package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.PhoneSampler
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.validation.UserValidator
import com.kaiqkt.services.authregistryservice.domain.validation.ValidationType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ValidationServiceTest {
    private val userValidator: UserValidator = mockk(relaxed = true)
    private val validation: ValidationService = ValidationService(userValidator)

    @Test
    fun `given a user, when is validated with success, should return nothing`() {
        assertDoesNotThrow { validation.validate(UserSampler.sample()) }
    }

    @Test
    fun `given a phone, when is validated with success, should return nothing`() {
        assertDoesNotThrow { validation.validate(PhoneSampler.sample()) }
    }

    @Test
    fun `given a object what not registered in validation service, should throw illegal argument exception`() {
        assertThrows<IllegalArgumentException> {
            validation.validate("Category.OTHERS")
        }
    }

    @Test
    fun `given a object, should throw validation exception when validation return error list`() {
        every { userValidator.validate(any<User>()) } returns mutableMapOf("email" to listOf(ValidationType.EMAIL_IN_USE))

        assertThrows<ValidationException> {
            validation.validate(UserSampler.sample())
        }
    }
}