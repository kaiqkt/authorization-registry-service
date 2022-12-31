package com.kaiqkt.services.authregistryservice.domain.entities

import com.kaiqkt.services.authregistryservice.application.controllers.APP_VERSION
import com.kaiqkt.services.authregistryservice.application.controllers.USER_AGENT

object DeviceSampler {
    fun sample() = Device(USER_AGENT, APP_VERSION)
}