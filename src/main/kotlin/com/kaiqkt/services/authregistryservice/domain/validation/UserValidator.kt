package com.kaiqkt.services.authregistryservice.domain.validation

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val userRepository: UserRepository
) {

    fun validate(
        user: User
    ): MutableMap<String, ValidationType> {
        val errors = mutableMapOf<String, ValidationType>()

        if (userRepository.existsByEmail(user.email)) {
            errors["email"] = ValidationType.EMAIL_IN_USE
        }

        return validate(user.phone, errors)
    }

    fun validate(
        phone: Phone,
        errors: MutableMap<String, ValidationType> = mutableMapOf()
    ): MutableMap<String, ValidationType> {

        if (!isValidPhone(phone)) {
            errors["phone"] = ValidationType.INVALID_PHONE
        }
        if (userRepository.existsByPhone(phone)) {
            errors["phone"] = ValidationType.PHONE_IN_USE
        }

        return errors
    }


    private fun isValidPhone(phone: Phone): Boolean {
        val phoneNumber = Phonenumber.PhoneNumber().apply {
            this.countryCode = phone.countryCode.toInt()
            this.nationalNumber = (phone.areaCode + phone.number).toLong()
        }

        return PhoneNumberUtil.getInstance().isValidNumber(phoneNumber)
    }
}