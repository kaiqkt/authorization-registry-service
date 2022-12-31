package com.kaiqkt.services.authregistryservice.domain.repositories

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.UpdateAddress

interface UserRepositoryCustom {
    fun createAddress(userId: String, address: Address)
    fun deleteAddress(userId: String, addressId: String)
    fun updateAddress(userId: String, addressId: String, updateAddress: UpdateAddress)
    fun updatePassword(userId: String, newPassword: Password)
    fun updatePhone(userId: String, phone: Phone)
}