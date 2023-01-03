package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.services.authregistryservice.domain.entities.Session
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class SessionRepositoryImpl(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${session-expiration}")
    private val sessionExpiration: String
) : SessionRepository {

    private val hashOperations = redisTemplate.opsForHash<String, String>()

    override fun save(session: Session) {
        val key = generateSessionKey(session.userId)
        try {
            hashOperations.put(key, session.id, session.toJson())
            redisTemplate.expire(key, sessionExpiration.toLong(), TimeUnit.DAYS)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to create session ${session.id}"
            )
        }
    }

    override fun delete(sessionId: String, userId: String) {
        val key = generateSessionKey(userId)
        try {
            hashOperations.delete(key, sessionId)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete session $sessionId"
            )
        }
    }

    override fun findByIdAndUserId(sessionId: String, userId: String): Session? {
        val key = generateSessionKey(userId)
        try {
            val json = hashOperations.get(key, sessionId) ?: return null
            return Session.toSession(json)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to find session $sessionId"
            )
        }
    }

    override fun findAllUserId(userId: String): List<Session> {
        val key = generateSessionKey(userId)
        return try {
            hashOperations.entries(key).values.map { Session.toSession(it) }
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to get sessions"
            )
        }
    }

    override fun deleteAllByUserId(userId: String) {
        val key = generateSessionKey(userId)
        try {
            redisTemplate.delete(key)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete sessions"
            )
        }
    }

    override fun deleteAllByUserIdExceptCurrent(sessionId: String, userId: String) {
        val key = generateSessionKey(userId)
        val sessionHashKeys = findAllUserId(userId)
            .filterNot { it.id == sessionId }
        try {
            if (sessionHashKeys.isNotEmpty()) {
                sessionHashKeys.map {
                    hashOperations.delete(key, it.id)
                }
            }
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete sessions"
            )
        }
    }

    private fun generateSessionKey(userId: String) = "USER_SESSION:$userId"
}