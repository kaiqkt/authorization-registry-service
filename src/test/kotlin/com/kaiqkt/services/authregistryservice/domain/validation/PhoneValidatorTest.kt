package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.PhoneSampler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PhoneValidatorTest {
    private val validator: PhoneValidator = PhoneValidator()

    @Test
    fun `given a phone, when is validated successfully, should not return error message`() {
        val phone = PhoneSampler.sample()

        val validation = validator.validate(phone)

        Assertions.assertNull(validation.errorMessage)
    }

    @Test
    fun `given a invalid phone, should return a list of validations with errors`() {
        val phone = PhoneSampler.sampleWithInvalidNumber()
        val prefix = phone.javaClass.simpleName.lowercase()

        val expectedError = Validation(
            fieldName = prefix,
            fieldValue = phone,
            errorMessage = "Invalid phone number"
        )

        val validation = validator.validate(phone)

        Assertions.assertEquals(expectedError, validation)
    }
}