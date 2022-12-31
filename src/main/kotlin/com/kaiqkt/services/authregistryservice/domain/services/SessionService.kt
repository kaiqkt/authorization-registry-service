package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.Session
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import io.azam.ulidj.ULID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {

    fun save(sessionId: String?, userId: String, device: Device, refreshToken: String): Session =
        Session(
            id = sessionId ?: ULID.random(),
            userId = userId,
            device = device,
            refreshToken = refreshToken
        ).also {
            sessionRepository.save(it)
            logger.info("Session ${it.id} for user ${it.userId} saved successfully")
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