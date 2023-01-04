package com.kaiqkt.services.authregistryservice.domain.repositories

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Phone

interface UserRepositoryCustom {
    fun createAddress(userId: String, address: Address)
    fun deleteAddress(userId: String, addressId: String)
    fun updateAddress(userId: String, address: Address)
    fun updatePassword(userId: String, newPassword: Password)
    fun updatePhone(userId: String, phone: Phone)
}