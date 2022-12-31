package com.kaiqkt.services.authregistryservice.resources.communication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "communication.email")
@ConstructorBinding
@Validated
data class EmailTemplateProvider(
    val locationTemplate: String,
    val redefinePasswordTemplate: String,
    val passwordUpdatedTemplate: String,
    val welcomeTemplate: String,
    val newAccessTemplate: String
)
