package com.kaiqkt.services.authregistryservice.domain.exceptions

class SessionNotFoundException(private val sessionId: String, private val userId: String)
    : DomainException("Session $sessionId for user $userId not found")