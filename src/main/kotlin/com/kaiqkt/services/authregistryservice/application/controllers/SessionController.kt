package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.security.auth.AUTHORIZE_USER
import com.kaiqkt.commons.security.auth.getSessionId
import com.kaiqkt.commons.security.auth.getUserId
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.services.SessionService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.SessionApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.SessionResponseV1
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionController(
    private val sessionService: SessionService
) : SessionApi {

    @PreAuthorize(AUTHORIZE_USER)
    override fun findAllByUserId(): ResponseEntity<List<SessionResponseV1>> {
        sessionService.findAllByUserId(getUserId())
            .map { it.toV1(getSessionId()) }
            .also { return ResponseEntity.ok(it) }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun exists(): ResponseEntity<Unit> {
        sessionService.findByIdAndUserId(getSessionId(), getUserId()).also {
            return ResponseEntity.noContent().build()
        }
    }
}