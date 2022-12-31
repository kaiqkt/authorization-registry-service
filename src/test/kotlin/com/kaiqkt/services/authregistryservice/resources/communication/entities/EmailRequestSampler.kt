package com.kaiqkt.services.authregistryservice.resources.communication.entities

object EmailRequestSampler {
    fun passwordResetEmailSample() = EmailRequest(
        subject = "Password reset request",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/password-reset.html",
            data = mapOf("name" to "shinji", "code" to "1234")
        )
    )

    fun welcomeEmailSample() = EmailRequest(
        subject = "Welcome to us site",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/welcome.html",
            data = mapOf("name" to "shinji")
        )
    )

    fun passwordUpdatedSample() = EmailRequest(
        subject = "Your password has been updated",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/password-updated.html",
            data = mapOf("name" to "shinji")
        )
    )

    fun newAccessSample() = EmailRequest(
        subject = "New device logged",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/new-access.html",
            data = mapOf("name" to "shinji")
        )
    )
}