package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.crypto.random.generateRandomString
import com.kaiqkt.services.authregistryservice.application.dto.AddressV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.NewPasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.PhoneV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordRequestV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.UpdateAddressV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.UserV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.AuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AddressNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ValidationException
import com.kaiqkt.services.authregistryservice.domain.services.RedefinePasswordService
import com.kaiqkt.services.authregistryservice.domain.services.UserService
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

class UserControllerTest {

    private val userService: UserService = mockk(relaxed = true)
    private val redefinePasswordService: RedefinePasswordService = mockk(relaxed = true)
    private val controller: UserController = UserController(userService, redefinePasswordService)

    @Test
    fun `given a request to create a user, when successfully created, should return http status 200`() {
        val request = UserV1Sampler.sample()
        val authentication = AuthenticationSampler.sample()
        val device = DeviceSampler.sample()

        every { userService.create(any(), any()) } returns authentication

        val response = controller.create(USER_AGENT, APP_VERSION, request)

        verify { userService.create(device, any()) }

        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertNotNull(response)
    }

    @Test
    fun `given a request to create a user, when validation return some invalid field, should throw ValidationException`() {
        val request = UserV1Sampler.sample()

        every {
            userService.create(
                any(),
                any()
            )
        } throws ValidationException(mapOf("email" to "Email already in use"))

        assertThrows<ValidationException> {
            controller.create(USER_AGENT, APP_VERSION, request)
        }
    }

    @Test
    fun `given a request to find a user, when the user exists, should return his information and http status 200`() {
        val user = UserSampler.sample()

        every { userService.findById(any()) } returns user

        val response = controller.findById(user.id)

        verify { userService.findById(user.id) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
    }

    @Test
    fun `given a request to get and user, when given a user id not exist in database, should throw UserNotFoundException`() {
        val userId = ULID.random()

        every { userService.findById(any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.findById(userId)
        }

        verify { userService.findById(userId) }
    }

    @Test
    fun `given a request to find a user based on access token, when the user exists, should return his information and http status 200`() {
        val user = UserSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.findById(any()) } returns user

        val response = controller.findByAccessToken()

        verify { userService.findById(user.id) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
    }

    @Test
    fun `given a request to get and user based on access token, when given a user id not exist in database, should throw UserNotFoundException`() {
        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.findById(any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.findByAccessToken()
        }

        verify { userService.findById(userId) }
    }

    @Test
    fun `given a request to update password based an actual password, when the user exists and the actual password matches, should revoke all sessions except the actual and return http status 204`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePassword(any(), any(), any(), any(), any()) } just runs

        val response = controller.updatePassword(USER_AGENT, APP_VERSION, password)

        verify {
            userService.updatePassword(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a request to update password based an actual password, when the user not exists, should throw UserNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePassword(any(), any(), any(), any(), any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.updatePassword(USER_AGENT, APP_VERSION, password)
        }

        verify {
            userService.updatePassword(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }
    }

    @Test
    fun `given a request to update password based an actual password, when the actual password not matches, should throw BadCredentialsException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePassword(any(), any(), any(), any(), any()) } throws BadCredentialsException()

        assertThrows<BadCredentialsException> {
            controller.updatePassword(USER_AGENT, APP_VERSION, password)
        }

        verify {
            userService.updatePassword(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }
    }

    @Test
    fun `given a request to redefine password, when the user exist, should send the email with the code to redefine the password and return http status 204`() {
        val request = RedefinePasswordRequestV1Sampler.sample()

        every { redefinePasswordService.send(any()) } just runs

        val response = controller.sendRedefinePasswordCode(request)

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify { redefinePasswordService.send(request.email) }
    }

    @Test
    fun `given a redefine code and the new password, when the user exist and the code matches, should persist the new password and return http status 204`() {
        val code = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { userService.redefinePassword(any(), any()) } just runs

        val response = controller.redefinePassword(request)

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify { userService.redefinePassword(request.code, request.newPassword) }
    }

    @Test
    fun `given a redefine code to validate, when exists and is not expired, should return http status 204`() {
        val code = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.isValidCode(any(), any()) } returns ULID.random()

        val response = controller.validateRedefinePasswordCode(request.code)

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify { redefinePasswordService.isValidCode(request.code, true) }
    }

    @Test
    fun `given a redefine code to validate, when exists and is expired, should throw InvalidResetPasswordException`() {
        val code = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.isValidCode(any(), any()) } throws InvalidRedefinePasswordException()

        assertThrows<InvalidRedefinePasswordException> {
            controller.validateRedefinePasswordCode(request.code)
        }

        verify { redefinePasswordService.isValidCode(request.code, true) }
    }

    @Test
    fun `given a redefine code to validate, when not exists, should throw InvalidResetPasswordException`() {
        val code = generateRandomString()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.isValidCode(any(), any()) } throws InvalidRedefinePasswordException()

        assertThrows<InvalidRedefinePasswordException> {
            controller.validateRedefinePasswordCode(request.code)
        }

        verify { redefinePasswordService.isValidCode(request.code, true) }
    }

    @Test
    fun `given a phone to update, should update and return http 204`() {
        val phoneV1 = PhoneV1Sampler.sample()
        val user = UserSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePhone(any(), any()) } just runs

        val response = controller.updatePhone(phoneV1)

        verify { userService.updatePhone(user.id, phoneV1.toDomain()) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a address to update, when find the user and exist the address, should update and return http 204`() {
        val updateAddress = UpdateAddressV1Sampler.sample()
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updateAddress(any(), any(), any()) } just runs

        val response = controller.updateAddress(addressId, updateAddress)

        verify { userService.updateAddress(user.id, addressId, updateAddress.toDomain()) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a address to update, when not find the user, should throw UserNotFoundException`() {
        val updateAddress = UpdateAddressV1Sampler.sample()
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updateAddress(any(), any(), any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.updateAddress(addressId, updateAddress)
        }

        verify { userService.updateAddress(user.id, addressId, updateAddress.toDomain()) }
    }

    @Test
    fun `given a address to update, when not find the address, should throw AddressNotFoundException`() {
        val updateAddress = UpdateAddressV1Sampler.sample()
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updateAddress(any(), any(), any()) } throws AddressNotFoundException()

        assertThrows<AddressNotFoundException> {
            controller.updateAddress(addressId, updateAddress)
        }

        verify { userService.updateAddress(user.id, addressId, updateAddress.toDomain()) }
    }

    @Test
    fun `given a address to delete, should delete successfully and return http status 204`() {
        val user = UserSampler.sample()
        val addressId = user.addresses.first().id

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.deleteAddress(any(), any()) } just runs

        val response = controller.deleteAddress(addressId)

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify { userService.deleteAddress(user.id, addressId) }
    }

    @Test
    fun `given a address to create, should create successfully successfully and return http status http 204`() {
        val user = UserSampler.sample()
        val newAddress = AddressV1Sampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.createAddress(any(), any()) } just runs

        val response = controller.createAddress(newAddress)

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify { userService.createAddress(user.id, any()) }
    }
}

