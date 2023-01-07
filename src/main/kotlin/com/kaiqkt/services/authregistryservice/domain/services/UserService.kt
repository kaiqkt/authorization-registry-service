package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.AddressNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val validationService: ValidationService,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val authenticationService: AuthenticationService,
    private val sessionService: SessionService,
    private val redefinePasswordService: RedefinePasswordService
) {

    fun create(device: Device, user: User): Authentication {
        logger.info("Creating user ${user.id}")

        validationService.validate(user)

        userRepository.save(user).run {
            logger.info("User ${user.id} created successfully")
            emailService.sendWelcomeEmail(this)
            return authenticationService.authenticate(this, device)
        }
    }

    fun createAddress(userId: String, address: Address) {
        userRepository.createAddress(userId, address)
        logger.info("Address ${address.id} for user $userId created successfully")
    }

    fun deleteAddress(userId: String, addressId: String) {
        userRepository.deleteAddress(userId, addressId)
        logger.info("Delete address $addressId for user $userId")
    }

    fun updateAddress(userId: String, address: Address) {
        runCatching { userRepository.updateAddress(userId, address) }
            .onFailure { throw AddressNotFoundException() }
            .also { logger.info("Address ${address.id} of user $userId updated successfully") }
    }

    fun updatePhone(userId: String, phone: Phone) {
        userRepository.updatePhone(userId, phone)
        logger.info("Phone of user $userId updated successfully")
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun findById(userId: String): User = userRepository.findById(userId).getOrNull() ?: throw UserNotFoundException()

    fun findByEmail(email: String): User = userRepository.findByEmail(email) ?: throw UserNotFoundException()

    fun updatePassword(
        actualPassword: String,
        newPassword: String,
        userId: String,
        sessionId: String,
        device: Device
    ) {
        val user = findById(userId)

        if (!EncryptUtils.validatePassword(actualPassword, user.password)) {
            throw BadCredentialsException()
        }

        sessionService.revokeAllExceptCurrent(sessionId, userId)

        userRepository.updatePassword(
            userId,
            EncryptUtils.encryptPassword(newPassword)
        )

        emailService.sendPasswordUpdatedEmail(user)

        logger.info("User ${user.id} updated password successfully")
    }

    fun redefinePassword(code: String, newPassword: String) {
        val userId = redefinePasswordService.isValidCode(code)
        val user = findById(userId)

        userRepository.updatePassword(
            userId,
            EncryptUtils.encryptPassword(newPassword)
        )

        sessionService.revokeAll(userId)

        emailService.sendPasswordUpdatedEmail(user)

        logger.info("User ${user.id} updated password successfully")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}