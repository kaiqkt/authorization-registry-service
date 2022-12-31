package com.kaiqkt.services.authregistryservice.domain.entities

import com.kaiqkt.commons.crypto.encrypt.Password
import io.azam.ulidj.ULID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class User(
    @Id
    val id: String = ULID.random(),
    val fullName: String,
    val email: String,
    val phone: Phone,
    val birthDate: String,
    val genre: Genre,
    val addresses: MutableList<Address> = mutableListOf(),
    val password: Password,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
){
    val firstName: String
        get() = fullName.substringBefore(" ")
}
