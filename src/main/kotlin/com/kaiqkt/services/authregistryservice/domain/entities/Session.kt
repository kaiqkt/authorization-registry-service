package com.kaiqkt.services.authregistryservice.domain.entities

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.azam.ulidj.ULID
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.LocalDateTime

data class Session(
    val id: String = ULID.random(),
    val userId: String,
    val device: Device,
    var refreshToken: String,
    var activeAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Serializable {

    fun toJson(): String = jacksonObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(this)

    companion object {
        fun toSession(json: String): Session =
            jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(json, Session::class.java)
    }
}