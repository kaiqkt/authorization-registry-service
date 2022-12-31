package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AddressValidatorTest {
    private val validator = AddressValidator()

    @Test
    fun `given a address, when is validated successfully, should not return error message`() {
        val address = AddressSampler.sample()

        val validation = validator.validate(address)

        Assertions.assertNull(validation.errorMessage)
    }

    @Test
    fun `given a invalid address, should return validation with error message list`() {
        val address = AddressSampler.sampleWithInvalidStreet()

        val expectedError = Validation(
            fieldName = "country",
            fieldValue = address.country,
            errorMessage = "Must have to be a valid country"
        )

        val validation = validator.validate(address)

        Assertions.assertEquals(expectedError, validation)
    }
}