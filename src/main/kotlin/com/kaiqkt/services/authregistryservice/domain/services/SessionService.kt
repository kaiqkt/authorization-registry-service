package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.Session
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {

    fun save(userId: String, device: Device, refreshToken: String): Session {
        val session = Session(
            userId = userId,
            device = device,
            refreshToken = refreshToken
        )

        sessionRepository.save(session)

        logger.info("Session ${session.id} for user $userId persisted successfully")

        return session
    }

    fun update(sessionId: String, userId: String, refreshToken: String): Session {
        findByIdAndUserId(sessionId, userId).apply {
            this.refreshToken = refreshToken
            this.activeAt = LocalDateTime.now()
        }.run {
            sessionRepository.save(this)
            logger.info("Session $sessionId for user $userId updated successfully")

            return this
        }
    }

    fun revoke(sessionId: String, userId: String) {
        sessionRepository.delete(sessionId, userId)

        logger.info("Session $sessionId for user $userId revoked successfully")
    }

    fun findByIdAndUserId(sessionId: String, userId: String): Session =
        sessionRepository.findByIdAndUserId(sessionId, userId) ?: throw SessionNotFoundException(sessionId, userId)

    fun findAllByUserId(userId: String): List<Session> = sessionRepository.findAllUserId(userId)

    fun revokeAll(userId: String) =
        sessionRepository.deleteAllByUserId(userId).also {
            logger.info("Revoked all sessions for user $userId")
        }

    fun revokeAllExceptCurrent(sessionId: String, userId: String) =
        sessionRepository.deleteAllByUserIdExceptCurrent(sessionId, userId).also {
            logger.info("Revoked all sessions for user $userId except the session $sessionId")
        }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}