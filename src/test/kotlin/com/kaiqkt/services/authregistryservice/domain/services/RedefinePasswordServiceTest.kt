package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RedefinePasswordServiceTest {
    private val redefinePasswordRepository: RedefinePasswordRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val redefinePasswordService: RedefinePasswordService = RedefinePasswordService(
        redefinePasswordRepository,
        userRepository,
        emailService
    )

    @Test
    fun `given a request o generate password redefine code, should send a email with him`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns user
        every { redefinePasswordRepository.save(any(), any()) } just runs
        every { emailService.sendPasswordResetEmail(any(), any()) } just runs

        redefinePasswordService.send(user.email)

        verify { userRepository.findByEmail(user.email) }
        verify { redefinePasswordRepository.save(any(), user.id) }
        verify { emailService.sendPasswordResetEmail(any(), any()) }
    }

    @Test
    fun `given a request o generate password redefine code, when the user not exist, should throw UserNotFoundException`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns null
        every { redefinePasswordRepository.save(any(), any()) } just runs
        every { emailService.sendPasswordResetEmail(any(), any()) } just runs

        assertThrows<UserNotFoundException> {
            redefinePasswordService.send(user.email)
        }

        verify { userRepository.findByEmail(user.email) }
        verify(exactly = 0) { redefinePasswordRepository.save(any(), user.id) }
        verify(exactly = 0) { emailService.sendPasswordResetEmail(any(), any()) }
    }

    @Test
    fun `given a code, when is not exist, should throw InvalidResetPasswordException`() {
        val redefinePasswordCode = generateRandomString()

        every { redefinePasswordRepository.findByCode(any()) } returns null

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordService.isValidCode(redefinePasswordCode)
        }

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist and is expired, should throw InvalidResetPasswordException and delete him`() {
        val redefinePasswordCode = generateRandomString()

        every { redefinePasswordRepository.findByCode(any()) } returns null
        every { redefinePasswordRepository.delete(any()) } just runs

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordService.isValidCode(redefinePasswordCode)
        }

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist, is not expired and is not validation request, should delete and return the user who use this code`() {
        val redefinePasswordCode = generateRandomString()
        val userId = ULID.random()

        every { redefinePasswordRepository.findByCode(any()) } returns userId
        every { redefinePasswordRepository.delete(any()) } just runs

        redefinePasswordService.isValidCode(redefinePasswordCode)

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
        verify { redefinePasswordRepository.delete(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist, is not expired and is validation request, should return the user who use this code`() {
        val redefinePasswordCode = generateRandomString()
        val userId = ULID.random()

        every { redefinePasswordRepository.findByCode(any()) } returns userId

        redefinePasswordService.isValidCode(redefinePasswordCode, true)

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }
}