package com.kaiqkt.services.authregistryservice.resources.communication

import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailRequest
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailType
import com.kaiqkt.services.authregistryservice.resources.communication.entities.Template
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@EnableConfigurationProperties(*[EmailTemplateProvider::class])
@Component
class CommunicationServiceImplementation(
    private val communicationClient: CommunicationClient,
    private val emailTemplatesProvider: EmailTemplateProvider,
) : CommunicationService {

    override fun sendEmail(recipient: String, templateData: Map<String, String>, emailType: EmailType) {
        val emailRequest = EmailRequest(
            recipient = recipient, subject = emailType.title,
            template = Template(
                url = "${emailTemplatesProvider.locationTemplate}${getTemplateUrl(emailType)}",
                data = templateData
            )
        )

        communicationClient.sendEmail(emailRequest)
    }

    private fun getTemplateUrl(emailType: EmailType): String {

        return when (emailType) {
            EmailType.PASSWORD_RESET_REQUEST -> emailTemplatesProvider.redefinePasswordTemplate
            EmailType.WELCOME_TEMPLATE -> emailTemplatesProvider.welcomeTemplate
            EmailType.PASSWORD_UPDATED -> emailTemplatesProvider.passwordUpdatedTemplate
            EmailType.NEW_ACCESS -> emailTemplatesProvider.newAccessTemplate
        }
    }
}