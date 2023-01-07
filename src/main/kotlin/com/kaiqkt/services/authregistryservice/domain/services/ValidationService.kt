package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.validation.UserValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ValidationService(
    private val userValidator: UserValidator
) {

    fun validate(obj: Any) {
        val validation = when (obj) {
            is User -> userValidator.validate(obj)
            is Phone -> userValidator.validate(obj)
            else -> throw IllegalArgumentException("Can not be found a validator for ${obj::class}")
        }

        if (validation.isNotEmpty()) {
            throw ValidationException(validation).also {
                logger.error("Invalid field value: $validation")
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}