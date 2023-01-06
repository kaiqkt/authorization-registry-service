package com.kaiqkt.services.authregistryservice.domain.exceptions

class InvalidRedefinePasswordException :
    DomainException(ErrorType.INVALID_REDEFINE_PASSWORD_CODE, "Reset code not exist or is expired")