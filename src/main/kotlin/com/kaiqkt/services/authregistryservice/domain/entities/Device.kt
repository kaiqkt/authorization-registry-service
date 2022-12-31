package com.kaiqkt.services.authregistryservice.domain.entities

import ua_parser.Parser
import java.io.Serializable


data class Device(
    val os: String,
    val osVersion: String,
    val model: String,
    val appVersion: String
) : Serializable {
    val defaultModel: String = if (this.model != "OTHER") this.model else this.os

    constructor(userAgent: String, appVersion: String) : this(
        os = Parser().parse(userAgent).os.family,
        osVersion = Parser().parse(userAgent).os.major,
        model = Parser().parse(userAgent).device.family,
        appVersion = appVersion
    )
}
