package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedefinePasswordRepositoryImpl(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${redefine-password-code-expiration}")
    private val redefinePasswordCodeExpiration: String
) : RedefinePasswordRepository {

    private val hashOperations = redisTemplate.opsForValue()

    override fun save(code: String, userId: String) {
        val key = generateSessionKey(code)
        try {
            hashOperations.set(key, userId)
            redisTemplate.expire(key, redefinePasswordCodeExpiration.toLong(), TimeUnit.MINUTES)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to create redefine password code to $userId"
            )
        }
    }

    override fun findByCode(code: String): String? {
        val key = generateSessionKey(code)
        try {
            return hashOperations.get(key)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to get redefine password code"
            )
        }
    }

    override fun delete(code: String) {
        val key = generateSessionKey(code)
        try {
            redisTemplate.delete(key)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete redefine password code"
            )
        }
    }

    private fun generateSessionKey(code: String) = "REDEFINE_PASS_CODE:$code"
}