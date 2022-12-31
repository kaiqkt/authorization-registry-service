package com.kaiqkt.services.authregistryservice.resources.communication.helpers

import com.kaiqkt.services.authregistryservice.holder.MockServerHolder

object CommunicationServiceMock : MockServerHolder() {
    val sendEmail = SendEmailPathMock(this)
}
