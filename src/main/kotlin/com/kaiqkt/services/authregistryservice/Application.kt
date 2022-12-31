package com.kaiqkt.services.authregistryservice

import com.kaiqkt.commons.health.COMMONS_HEALTH
import com.kaiqkt.commons.security.COMMONS_SECURITY
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.kaiqkt.services.authregistryservice", COMMONS_SECURITY, COMMONS_HEALTH])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
