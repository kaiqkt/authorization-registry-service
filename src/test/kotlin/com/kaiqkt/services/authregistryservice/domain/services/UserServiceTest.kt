package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.AuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.PhoneSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AddressNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import com.kaiqkt.services.authregistryservice.domain.validation.ValidationType
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserServiceTest {
    private val validationService: ValidationService = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val authenticationService: AuthenticationService = mockk(relaxed = true)
    private val sessionService: SessionService = mockk(relaxed = true)
    private val redefinePasswordService: RedefinePasswordService = mockk(relaxed = true)
    private val userService: UserService =
        UserService(
            validationService,
            userRepository,
            emailService,
            authenticationService,
            sessionService,
            redefinePasswordService
        )

    @Test
    fun `given a new user, when validated with successfully, should persist in database, send welcome email and return authentication`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { validationService.validate(any()) } just runs
        every { userRepository.save(any()) } returns user
        every { emailService.sendWelcomeEmail(any()) } just runs
        every {
            authenticationService.authenticate(
                any(),
                any()
            )
        } returns AuthenticationSampler.sample()

        userService.create(device, user)

        verify { validationService.validate(user) }
        verify { userRepository.save(user) }
        verify { emailService.sendWelcomeEmail(user) }
        verify { authenticationService.authenticate(user, device) }
    }

    @Test
    fun `given a new user, when validation return some invalid field, should throw ValidationException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { validationService.validate(any()) } throws ValidationException(mutableMapOf("email" to ValidationType.EMAIL_IN_USE))

        assertThrows<ValidationException> {
            userService.create(device, user)
        }
    }


    @Test
    fun `given a new user, when fail to create authentication session, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { validationService.validate(any()) } just runs
        every { userRepository.save(any()) } returns user
        every { emailService.sendWelcomeEmail(any()) } just runs
        every {
            authenticationService.authenticate(
                any(),
                any()
            )
        } throws PersistenceException("Unable to persist session 209310LDFL")

        assertThrows<PersistenceException> {
            userService.create(device, user)
        }
    }


    @Test
    fun `given find user, when user is exist in database, should return his`() {
        val user = UserSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)

        userService.findById(user.id)

        verify { userRepository.findById(user.id) }
    }

    @Test
    fun `given an address to create, should persist successfully`() {
        val user = UserSampler.sample()
        val address = user.addresses.first()

        every { userRepository.createAddress(any(), any()) } just runs

        userService.createAddress(user.id, address)

        every { userRepository.createAddress(user.id, address) } just runs
    }

    @Test
    fun `given an address to delete, should delete successfully`() {
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        every { userRepository.deleteAddress(any(), any()) } just runs

        userService.deleteAddress(user.id, addressId)

        every { userRepository.deleteAddress(user.id, addressId) } just runs
    }

    @Test
    fun `given an address to update, should update the fields which is not null`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.updateAddress(any(), any()) } just runs

        userService.updateAddress(user.id, address)

        every { userRepository.updateAddress(user.id, address) } just runs
    }

    @Test
    fun `given an phone to update, should update with the phone update successfully`() {
        val user = UserSampler.sample()
        val phone = PhoneSampler.sample()

        every { userRepository.updatePhone(any(), any()) } just runs

        userService.updatePhone(user.id, phone)

        verify { userRepository.updatePhone(user.id, phone) }
    }

    @Test
    fun `given an address to update, when is all null, should update with the values with is already saved`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.updateAddress(any(), any()) } just runs

        userService.updateAddress(user.id, address)

        verify { userRepository.updateAddress(user.id, address) }
    }

    @Test
    fun `given an address to update, when not have the address, should throw AddressNotFoundException`() {
        val user = UserSampler.sample()
        val address = AddressSampler.sample()

        every { userRepository.updateAddress(any(), any()) } throws Exception()

        assertThrows<AddressNotFoundException> {
            userService.updateAddress(user.id, address)
        }

        verify { userRepository.updateAddress(user.id, address) }
    }


    @Test
    fun `given find user, when user not exists in database, should throws UserNotFoundException`() {
        val userId = ULID.random()

        every { userRepository.findById(any()) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            userService.findById(userId)
        }

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `given an user email, when find user successfully, should return him`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns user

        userService.findByEmail(user.email)

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given an user email, when user not exists, should throw UserNotFoundException`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns null

        assertThrows<UserNotFoundException> {
            userService.findByEmail(user.email)
        }

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given a password to redefine, when the actual password matches, should update successfully`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()
        val newPassword = "12345678"

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.revokeAllExceptCurrent(any(), any()) } just runs
        every { userRepository.updatePassword(any(), any()) } just runs

        userService.updatePassword("1234657", newPassword, user.id, session.id, device)

        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify { userRepository.updatePassword(user.id, any()) }
    }

    @Test
    fun `given a password to redefine, when the actual password not matches, should throw BadCredentialsException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.revokeAllExceptCurrent(any(), any()) } just runs
        every { userRepository.save(any()) } returns user

        assertThrows<BadCredentialsException> {
            userService.updatePassword("1234658", "12345678", user.id, session.id, device)
        }

        verify { userRepository.findById(user.id) }
        verify(exactly = 0) { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `given a password to redefine, when fail to revoke the sessions, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every {
            sessionService.revokeAllExceptCurrent(
                any(),
                any()
            )
        } throws PersistenceException("Unable to delete sessions")
        every { userRepository.save(any()) } returns user

        assertThrows<PersistenceException> {
            userService.updatePassword("1234657", "12345678", user.id, session.id, device)
        }

        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `given password reset code and new password, should update the password and send password redefined email`() {
        val code = generateRandomString()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordService.isValidCode(any()) } returns user.id
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user
        every { sessionService.revokeAll(any()) } just runs
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        userService.redefinePassword(code, password)

        verify { redefinePasswordService.isValidCode(code) }
        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAll(user.id) }
        verify { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password reset code and new password, when not find the code, should throw ResetPasswordCodeNotFoundException`() {
        val code = generateRandomString()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordService.isValidCode(any()) } throws InvalidRedefinePasswordException()
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.revokeAll(any()) } just runs
        every { userRepository.save(any()) } returns user
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        assertThrows<InvalidRedefinePasswordException> {
            userService.redefinePassword(code, password)
        }

        verify { redefinePasswordService.isValidCode(code) }
        verify(exactly = 0) { userRepository.findById(user.id) }
        verify(exactly = 0) { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password reset code and new password, when not find the user, should throw UserNotFoundException`() {
        val code = generateRandomString()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordService.isValidCode(any()) } returns user.id
        every { userRepository.findById(any()) } returns Optional.empty()
        every { sessionService.revokeAll(any()) } just runs
        every { userRepository.save(any()) } returns user
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        assertThrows<UserNotFoundException> {
            userService.redefinePassword(code, password)
        }

        verify { redefinePasswordService.isValidCode(code) }
        verify { userRepository.findById(user.id) }
        verify(exactly = 0) { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password reset code and new password, when fail to revoke the sessions, should throw PersistenceException`() {
        val code = generateRandomString()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordService.isValidCode(any()) } returns user.id
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user
        every { sessionService.revokeAll(any()) } throws PersistenceException("Unable to delete all sessions")
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        assertThrows<PersistenceException> {
            userService.redefinePassword(code, password)
        }

        verify { redefinePasswordService.isValidCode(code) }
        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }
}