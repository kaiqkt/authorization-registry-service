package com.kaiqkt.services.authregistryservice.domain.exceptions

class UserNotFoundException : DomainException(ErrorType.USER_NOT_FOUND, "User not found")