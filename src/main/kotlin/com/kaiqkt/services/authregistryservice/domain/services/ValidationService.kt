package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import com.kaiqkt.services.authregistryservice.domain.validation.AddressValidator
import com.kaiqkt.services.authregistryservice.domain.validation.PhoneValidator
import com.kaiqkt.services.authregistryservice.domain.validation.UserValidator
import com.kaiqkt.services.authregistryservice.domain.validation.Validator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ValidationService(
    userRepository: UserRepository
) {
    private val validators = mutableMapOf<KClass<*>, Validator>()

    init {
        validators[Address::class] = AddressValidator()
        validators[Phone::class] = PhoneValidator()
        validators[User::class] = UserValidator(userRepository)
    }

    fun validate(obj: Any) {
        val validator = validators[obj::class]
            ?: throw IllegalArgumentException("Can not be found a validator for ${obj::class}")

        val validation = validator.validate(obj)

        if (validation.errorMessage != null) {
            val errors: Map<String, String> = mapOf(validation.fieldName to validation.errorMessage!!)
            throw ValidationException(errors).also {
                logger.error("Invalid field value: $errors")
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}