package com.kaiqkt.services.authregistryservice.domain.gateways

import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailType

interface CommunicationService {
    fun sendEmail(recipient: String, templateData: Map<String, String>, emailType: EmailType)
}