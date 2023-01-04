package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.security.auth.AUTHORIZE_USER
import com.kaiqkt.commons.security.auth.filter.BEARER_PREFIX
import com.kaiqkt.commons.security.auth.getSessionId
import com.kaiqkt.commons.security.auth.getUserId
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.services.AuthenticationService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.AuthApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.LoginV1
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController


@RestController
class  AuthenticationController(private val authenticationService: AuthenticationService) : AuthApi {

    @PreAuthorize(AUTHORIZE_USER)
    override fun logout(): ResponseEntity<Unit> {
        authenticationService.logout(getUserId(), getSessionId())
            .also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun logoutSessionById(sessionId: String): ResponseEntity<Unit> {
        authenticationService.logout(getUserId(), sessionId)
            .also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun logoutAllExceptCurrent(): ResponseEntity<Unit> {
        authenticationService.logoutAllExceptCurrent(getUserId(), getSessionId())
            .also { return ResponseEntity.noContent().build() }
    }

    override fun refresh(authorization: String, refreshToken: String): ResponseEntity<AuthenticationResponseV1> {
        val accessToken = authorization.replace(BEARER_PREFIX, "")
        authenticationService.refresh(accessToken, refreshToken).also { return ResponseEntity.ok(it.toV1()) }
    }

    override fun authenticate(
        userAgent: String,
        appVersion: String,
        loginV1: LoginV1
    ): ResponseEntity<AuthenticationResponseV1> {
        val device = Device(userAgent, appVersion)
        authenticationService.authenticateWithCredentials(device, loginV1.email, loginV1.password)
            .toV1()
            .also { return ResponseEntity.ok(it) }
    }
}