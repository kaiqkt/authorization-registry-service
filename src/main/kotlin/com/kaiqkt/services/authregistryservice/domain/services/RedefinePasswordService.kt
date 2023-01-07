package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RedefinePasswordService(
    private val redefinePasswordRepository: RedefinePasswordRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    fun send(email: String) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        val code = generateRandomString()

        redefinePasswordRepository.save(code, user.id)

        logger.info("Generated code to password reset for user ${user.id}")

        emailService.sendPasswordResetEmail(code, user)
    }


    fun isValidCode(code: String, isValidation: Boolean = false): String {
        redefinePasswordRepository.findByCode(code)?.let {
           return when {
                isValidation -> it
                else -> {
                    redefinePasswordRepository.delete(code)
                    it
                }
            }
        }
        throw InvalidRedefinePasswordException()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}