package com.kaiqkt.services.authregistryservice.domain.exceptions

class SessionNotFoundException
    : DomainException(ErrorType.SESSION_NOT_FOUND, "Session not found")