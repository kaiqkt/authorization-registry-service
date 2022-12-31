package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmailService(
    private val communicationService: CommunicationService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun sendNewAccessEmail(user: User, device: Device) {
        val templateData = mapOf(
            "device" to device.defaultModel
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            emailType = EmailType.NEW_ACCESS
        )
    }

    fun sendPasswordResetEmail(code: String, user: User) {
        val templateData = mapOf(
            "name" to user.firstName,
            "code" to code
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            emailType = EmailType.PASSWORD_RESET_REQUEST
        )
    }

    fun sendPasswordUpdatedEmail(user: User){
        val templateData = mapOf(
            "name" to user.firstName
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            emailType = EmailType.PASSWORD_UPDATED
        )
    }

    fun sendWelcomeEmail(user: User) {
        val templateData = mapOf(
            "name" to user.firstName
        )

        try {
            communicationService.sendEmail(
                recipient = user.email,
                templateData = templateData,
                emailType = EmailType.WELCOME_TEMPLATE
            )
        } catch (e: Exception) {
            logger.error("Failed to send welcome email to user ${user.id}")
        }
    }
}