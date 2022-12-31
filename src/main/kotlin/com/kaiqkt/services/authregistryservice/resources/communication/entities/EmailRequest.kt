package com.kaiqkt.services.authregistryservice.resources.communication.entities

data class EmailRequest(
    val subject: String,
    val recipient: String,
    val template: Template
)
