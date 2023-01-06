package com.kaiqkt.services.authregistryservice.domain.exceptions

class AddressNotFoundException : DomainException(ErrorType.ADDRESS_NOT_FOUND, "Address not found")