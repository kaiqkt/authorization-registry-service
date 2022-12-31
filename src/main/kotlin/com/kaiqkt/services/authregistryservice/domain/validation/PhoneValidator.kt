package com.kaiqkt.services.authregistryservice.domain.validation

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.kaiqkt.services.authregistryservice.domain.entities.Phone

class PhoneValidator : Validator {

    override fun validate(obj: Any): Validation<*> {
        val phone = obj as Phone
        return Validation("phone", phone).apply {
            if (!isValidPhone(phone)) {
                this.errorMessage = "Invalid phone number"
            }
        }
    }

    private fun isValidPhone(phone: Phone): Boolean {
        val util = PhoneNumberUtil.getInstance()

        val countryCode = phone.countryCode.replace("+", "", ignoreCase = false).toInt()
        val areaCode = phone.areaCode.replace("(", "", ignoreCase = false).replace(")", "", ignoreCase = false)
        val number = phone.number.replace("-", "", ignoreCase = false)

        val phoneNumber = Phonenumber.PhoneNumber().apply {
            this.countryCode = countryCode
            this.nationalNumber = (areaCode + number).toLong()
        }

        return util.isValidNumber(phoneNumber)
    }
}
