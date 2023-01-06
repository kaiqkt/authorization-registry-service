package com.kaiqkt.services.authregistryservice.domain.exceptions

class BadCredentialsException : DomainException(ErrorType.INCORRECT_PASSWORD, "Incorrect password")