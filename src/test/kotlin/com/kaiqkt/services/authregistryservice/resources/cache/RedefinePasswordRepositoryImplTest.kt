package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.commons.crypto.random.generateRandomString
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

class RedefinePasswordRepositoryImplTest{
    private val redisTemplate: StringRedisTemplate = mockk(relaxed = true)
    private val hashOperations = redisTemplate.opsForValue()
    private val expiration = "5"
    private val redefinePasswordRepositoryImpl: RedefinePasswordRepositoryImpl =
        RedefinePasswordRepositoryImpl(redisTemplate, expiration)

    @Test
    fun `given an redefine password code with expiration, should persist in database successfully`() {
        val code = generateRandomString()
        val userId = ULID.random()

        every { hashOperations.set(any(), any()) } just runs
        every { redisTemplate.expire(any(), any(), any()) } returns true

        redefinePasswordRepositoryImpl.save(code, userId)

        verify { hashOperations.set("REDEFINE_PASS_CODE:$code", userId) }
        verify { redisTemplate.expire("REDEFINE_PASS_CODE:$code", expiration.toLong(), TimeUnit.MINUTES) }
    }

    @Test
    fun `given an session with expiration,when fail to persist, should throw PersistenceException`() {
        val code = generateRandomString()
        val userId = ULID.random()

        every { hashOperations.set(any(), any()) } throws Exception()
        every { redisTemplate.expire(any(), any(), any()) } returns true

        assertThrows<PersistenceException> {
            redefinePasswordRepositoryImpl.save(code, userId)
        }

        verify { hashOperations.set("REDEFINE_PASS_CODE:$code", userId) }
        verify(exactly = 0) { redisTemplate.expire("REDEFINE_PASS_CODE:$code", expiration.toLong(), TimeUnit.MINUTES) }
    }

    @Test
    fun `given an code to get in cache,when exist, should return the user id for the code successfully`() {
        val code = generateRandomString()
        val userId = ULID.random()

        every { hashOperations.get(any()) } returns userId

        redefinePasswordRepositoryImpl.findByCode(code)

        verify { hashOperations.get("REDEFINE_PASS_CODE:$code") }
    }

    @Test
    fun `given an code to get in cache,when fail to get the user id, should throw PersistenceException`() {
        val code = generateRandomString()

        every { hashOperations.get(any()) } throws Exception()

        assertThrows<PersistenceException> {
            redefinePasswordRepositoryImpl.findByCode(code)
        }

        verify { hashOperations.get("REDEFINE_PASS_CODE:$code") }
    }

    @Test
    fun `given an code to delete, should delete successfully`() {
        val code = generateRandomString()

        every { redisTemplate.delete(any<String>()) } returns true

        redefinePasswordRepositoryImpl.delete(code)

        verify { redisTemplate.delete("REDEFINE_PASS_CODE:$code") }
    }

    @Test
    fun `given an code to delete, when fail to delete, should throw PersistenceException`() {
        val code = generateRandomString()

        every { redisTemplate.delete(any<String>()) } throws Exception()

        assertThrows<PersistenceException> {
            redefinePasswordRepositoryImpl.delete(code)
        }

        verify { redisTemplate.delete("REDEFINE_PASS_CODE:$code") }
    }
}