package com.kaiqkt.services.authregistryservice.domain.validation

import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository

class UserValidator(
    private val userRepository: UserRepository
) : Validator {
    override fun validate(obj: Any): Validation<*> {
        val user = obj as User
        return Validation("email", user.email).apply {
            if (userRepository.existsByEmail(user.email)) {
                this.errorMessage = "Email already in use"
            }
        }
    }
}