package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import java.util.Locale.IsoCountryCode.PART1_ALPHA2
import java.util.Locale.getISOCountries

class AddressValidator : Validator {

    override fun validate(obj: Any): Validation<String> {
        val address = obj as Address
        return Validation(fieldName = "country", fieldValue = address.country).apply {
            if (!hasCountry(address.country)) {
                this.errorMessage = "Must have to be a valid country"
            }
        }
    }

    private fun hasCountry(country: String) = getISOCountries(PART1_ALPHA2).contains(country)
}