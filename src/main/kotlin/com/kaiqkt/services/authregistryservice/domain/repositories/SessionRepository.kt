package com.kaiqkt.services.authregistryservice.domain.repositories

import com.kaiqkt.services.authregistryservice.domain.entities.Session
import org.springframework.stereotype.Repository

@Repository
interface SessionRepository {
    fun save(session: Session)
    fun delete(sessionId: String, userId: String)
    fun findByIdAndUserId(sessionId: String, userId: String): Session?
    fun findAllUserId(userId: String): List<Session>
    fun deleteAllByUserId(userId: String)
    fun deleteAllByUserIdExceptCurrent(sessionId: String, userId: String)
}