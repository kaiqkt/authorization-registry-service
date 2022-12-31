package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Session
import com.kaiqkt.services.authregistryservice.generated.application.dto.SessionResponseV1

fun Session.toV1(sessionId: String) = SessionResponseV1(
    device = this.device.defaultModel,
    sessionId = this.id,
    thisDevice = this.id == sessionId
)
