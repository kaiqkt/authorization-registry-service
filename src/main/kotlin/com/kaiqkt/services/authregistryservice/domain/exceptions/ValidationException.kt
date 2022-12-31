package com.kaiqkt.services.authregistryservice.domain.exceptions

class ValidationException(val errorDetails: Map<String, String>) : DomainException("Invalid field value")