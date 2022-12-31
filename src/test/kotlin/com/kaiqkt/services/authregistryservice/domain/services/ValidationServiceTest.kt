package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ValidationServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val validation: ValidationService = ValidationService(userRepository)

    @Test
    fun `given a object, when is validated with success, should return nothing`() {
        assertDoesNotThrow { validation.validate(AddressSampler.sample()) }
    }

    @Test
    fun `given a object what not registered in validation service, should throw illegal argument exception`() {
        assertThrows<IllegalArgumentException> {
            validation.validate("Category.OTHERS")
        }
    }

    @Test
    fun `given a object, should throw validation exception when validation return error list`() {
        assertThrows<ValidationException> {
            validation.validate(AddressSampler.sampleWithInvalidStreet())
        }
    }
}