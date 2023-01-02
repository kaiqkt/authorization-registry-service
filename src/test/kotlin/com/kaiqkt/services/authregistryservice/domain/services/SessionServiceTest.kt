package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SessionServiceTest {
    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private val sessionExpiration = "2"
    private val sessionService: SessionService = SessionService(sessionRepository)

    @Test
    fun `given a sessionId and userId, should delete the session successfully`() {
        val sessionId = ULID.random()
        val userId = ULID.random()

        every { sessionRepository.delete(any(), any()) } just runs

        sessionService.revoke(sessionId, userId)

        verify { sessionRepository.delete(sessionId, userId) }
    }

    @Test
    fun `given a sessionId and userId, when fail to delete, should throw PersistenceException`() {
        val sessionId = ULID.random()
        val userId = ULID.random()

        every { sessionRepository.delete(any(), any()) } throws PersistenceException("Unable to delete session")

        assertThrows<PersistenceException> {
            sessionService.revoke(sessionId, userId)
        }

        verify { sessionRepository.delete(sessionId, userId) }
    }

    @Test
    fun `given a session, should persist successfully`() {
        val userId = ULID.random()
        val device = DeviceSampler.sample()
        val refreshToken = generateRandomString()

        every { sessionRepository.save(any()) } just runs

        val session =
            sessionService.save(userId = userId, device = device, refreshToken = refreshToken)

        verify { sessionRepository.save(session) }
    }

    @Test
    fun `given a session to update, should give the session id and persist successfully`() {
        val sessionId = ULID.random()
        val userId = ULID.random()
        val refreshToken = generateRandomString()

        every { sessionRepository.save(any()) } just runs

        val session =
            sessionService.update(userId = userId, sessionId = sessionId, refreshToken = refreshToken)

        verify { sessionRepository.save(session) }
    }

    @Test
    fun `given a session, when fail to persist, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { sessionRepository.save(any()) } throws PersistenceException("Unable to save session")

        assertThrows<PersistenceException> {
            sessionService.save(
                userId = session.userId,
                device = session.device,
                refreshToken = session.refreshToken
            )
        }

        verify { sessionRepository.save(any()) }
    }

    @Test
    fun `given a sessionId and userId,when the session exist in redis, should return this successfully`() {
        val session = SessionSampler.sample()

        every { sessionRepository.findByIdAndUserId(any(), any()) } returns session

        sessionService.findByIdAndUserId(session.id, session.userId)

        verify { sessionRepository.findByIdAndUserId(session.id, session.userId) }
    }

    @Test
    fun `given a session,when the session not exists in redis, should throw null`() {
        val session = SessionSampler.sample()

        every { sessionRepository.findByIdAndUserId(any(), any()) } returns null

        assertThrows<SessionNotFoundException> {
            sessionService.findByIdAndUserId(session.id, session.userId)
        }

        verify { sessionRepository.findByIdAndUserId(session.id, session.userId) }
    }

    @Test
    fun `given a session,when fail to get the session, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every {
            sessionRepository.findByIdAndUserId(
                any(),
                any()
            )
        } throws PersistenceException("Unable to get the session")

        assertThrows<PersistenceException> {
            sessionService.findByIdAndUserId(session.id, session.userId)
        }

        verify { sessionRepository.findByIdAndUserId(session.id, session.userId) }
    }

    @Test
    fun `given a userId, when exist sessions for his, should return the sessions`() {
        val sessions = listOf(SessionSampler.sample())

        every { sessionRepository.findAllUserId(any()) } returns sessions

        sessionService.findAllByUserId("01GFPPTXKZ8ZJRG8MF701M0W99")

        verify { sessionRepository.findAllUserId("01GFPPTXKZ8ZJRG8MF701M0W99") }
    }

    @Test
    fun `given a userId, when fail to get the sessions, should throw PersistenceException `() {
        every { sessionRepository.findAllUserId(any()) } throws PersistenceException("Unable to persist session")

        assertThrows<PersistenceException> {
            sessionService.findAllByUserId("01GFPPTXKZ8ZJRG8MF701M0W99")
        }

        verify { sessionRepository.findAllUserId("01GFPPTXKZ8ZJRG8MF701M0W99") }
    }

    @Test
    fun `given a userId, should revoke all session successfully`() {
        val userId = ULID.random()

        every { sessionRepository.deleteAllByUserId(any()) } just runs

        sessionService.revokeAll(userId)

        verify { sessionRepository.deleteAllByUserId(userId) }
    }

    @Test
    fun `given a userId, when fail to delete, should throw PersistenceException`() {
        val userId = ULID.random()

        every { sessionRepository.deleteAllByUserId(any()) } throws PersistenceException("Unable to delete sessions")

        assertThrows<PersistenceException> {
            sessionService.revokeAll(userId)
        }

        verify { sessionRepository.deleteAllByUserId(userId) }
    }

    @Test
    fun `given a userId, should revoke all session except the current session successfully`() {
        val userId = ULID.random()
        val sessionId = ULID.random()

        every { sessionRepository.deleteAllByUserIdExceptCurrent(any(), any()) } just runs

        sessionService.revokeAllExceptCurrent(sessionId, userId)

        verify { sessionRepository.deleteAllByUserIdExceptCurrent(sessionId, userId) }
    }

    @Test
    fun `given a userId, when fail to delete all except the current, should throw PersistenceException`() {
        val userId = ULID.random()
        val sessionId = ULID.random()

        every {
            sessionRepository.deleteAllByUserIdExceptCurrent(
                any(),
                any()
            )
        } throws PersistenceException("Unable to delete sessions")

        assertThrows<PersistenceException> {
            sessionService.revokeAllExceptCurrent(sessionId, userId)
        }

        verify { sessionRepository.deleteAllByUserIdExceptCurrent(sessionId, userId) }
    }
}