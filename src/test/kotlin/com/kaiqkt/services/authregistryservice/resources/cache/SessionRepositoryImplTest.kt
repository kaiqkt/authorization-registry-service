package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

class SessionRepositoryImplTest {
    private val redisTemplate: StringRedisTemplate = mockk(relaxed = true)
    private val hashOperations = redisTemplate.opsForHash<String, String>()
    private val expiration = "1"
    private val sessionRepositoryImpl: SessionRepositoryImpl =
        SessionRepositoryImpl(redisTemplate, expiration)

    @Test
    fun `given an session with expiration, should persist in database successfully`() {
        val session = SessionSampler.sample()

        every { hashOperations.put(any(), any(), any()) } just runs
        every { redisTemplate.expire(any(), any(), any()) } returns true

        sessionRepositoryImpl.save(session)

        verify { hashOperations.put("USER_SESSION:${session.userId}", session.id, session.toJson()) }
        verify { redisTemplate.expire("USER_SESSION:${session.userId}", expiration.toLong(), TimeUnit.DAYS) }
    }

    @Test
    fun `given an session with expiration,when fail to persist, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.put(any(), any(), any()) } throws Exception()
        every { redisTemplate.expire(any(), any(), any()) } returns true

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.save(session)
        }

        verify { hashOperations.put("USER_SESSION:${session.userId}", session.id, session.toJson()) }
        verify(exactly = 0) { redisTemplate.expire(any(), any(), any()) }
    }

    @Test
    fun `given an session with expiration,when fail to persist the expiration, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.put(any(), any(), any()) } just runs
        every { redisTemplate.expire(any(), any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.save(session)
        }

        verify { hashOperations.put("USER_SESSION:${session.userId}", session.id, session.toJson()) }
        verify { redisTemplate.expire("USER_SESSION:${session.userId}", expiration.toLong(), TimeUnit.DAYS) }
    }

    @Test
    fun `given an sessionId and userId, should delete his session`() {
        val session = SessionSampler.sample()


        every { hashOperations.delete(any(), any()) } returns 1L

        sessionRepositoryImpl.delete(session.id, session.userId)

        verify { hashOperations.delete("USER_SESSION:${session.userId}", session.id) }
    }

    @Test
    fun `given an sessionId and userId, when fail to delete, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.delete(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.delete(session.id, session.userId)
        }
    }

    @Test
    fun `given an userId, should return his session`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } returns session.toJson()

        sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)

        verify { hashOperations.get(any(), any()) }
    }

    @Test
    fun `given an userId, when not exist, should return null`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } returns null

        sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)

        verify { hashOperations.get(any(), any()) }
    }

    @Test
    fun `given an userId, when fail to get the session, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)
        }
    }

    @Test
    fun `given a userId, should return his sessions`() {
        val session = SessionSampler.sample()

        every {
            hashOperations.entries(any())
        } returns mutableMapOf("USER_SESSION:${session.userId}" to session.toJson())

        sessionRepositoryImpl.findAllUserId(session.userId)

        verify { hashOperations.entries("USER_SESSION:${session.userId}") }
    }

    @Test
    fun `given a userId, when fail to get the sessions, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.entries(any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.findAllUserId(session.userId)
        }
    }

    @Test
    fun `given a user id, should delete all sessions based on userid successfully`() {
        val userId = ULID.random()

        every { redisTemplate.delete(any<String>()) } returns true

        sessionRepositoryImpl.deleteAllByUserId(userId)

        verify { redisTemplate.delete("USER_SESSION:$userId") }
    }

    @Test
    fun `given a user id, when fail to delete all sessions, should throw PersistenceException`() {
        val userId = ULID.random()

        every { redisTemplate.delete(any<String>()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.deleteAllByUserId(userId)
        }

        verify { redisTemplate.delete("USER_SESSION:$userId") }
    }


    @Test
    fun `given a user id, should delete a sessions based on userid except the actual successfully`() {
        val userId = ULID.random()
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())

        every {
            hashOperations.entries(any())
        } returns mutableMapOf(
            "USER_SESSION:${userId}" to session1.toJson(),
            "USER_SESSION:${userId}" to session2.toJson()
        )
        every { hashOperations.delete(any(), any()) } returns 1L

        sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session1.id, userId)

        verify { hashOperations.entries("USER_SESSION:$userId") }
        verify { hashOperations.delete("USER_SESSION:$userId", session2.id) }
    }

    @Test
    fun `given a user id, when there is no session other than the current one, should do nothing`() {
        val userId = ULID.random()
        val session = SessionSampler.sample()

        every {
            hashOperations.entries(any())
        } returns mutableMapOf("USER_SESSION:${userId}" to session.toJson())

        sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session.id, userId)

        verify { hashOperations.entries("USER_SESSION:$userId") }
        verify(exactly = 0) { hashOperations.delete("USER_SESSION:$userId", session.id) }
    }


    @Test
    fun `given a user id, when fail to delete all sessions except the current, should throw PersistenceException`() {
        val userId = ULID.random()
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())

        every {
            hashOperations.entries(any())
        } returns mutableMapOf(
            "USER_SESSION:${userId}" to session1.toJson(),
            "USER_SESSION:${userId}" to session2.toJson()
        )
        every { hashOperations.delete(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session1.id, userId)
        }

        verify { hashOperations.entries("USER_SESSION:$userId") }
        verify { hashOperations.delete("USER_SESSION:$userId", session2.id) }
    }
}