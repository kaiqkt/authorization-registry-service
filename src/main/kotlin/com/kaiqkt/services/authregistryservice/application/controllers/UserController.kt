package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.security.auth.AUTHORIZE_SERVICE
import com.kaiqkt.commons.security.auth.AUTHORIZE_USER
import com.kaiqkt.commons.security.auth.getSessionId
import com.kaiqkt.commons.security.auth.getUserId
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.services.RedefinePasswordService
import com.kaiqkt.services.authregistryservice.domain.services.UserService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.UserApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.AddressV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.NewPasswordV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.PhoneV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.RedefinePasswordRequestV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.RedefinePasswordV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UpdateAddressV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserResponseV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController


@RestController
class UserController(
    private val userService: UserService,
    private val redefinePasswordService: RedefinePasswordService
) : UserApi {

    @PreAuthorize(AUTHORIZE_USER)
    override fun updatePhone(phoneV1: PhoneV1): ResponseEntity<Unit> {
        userService.updatePhone(getUserId(), phoneV1.toDomain()).also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun updatePassword(
        userAgent: String,
        appVersion: String,
        newPasswordV1: NewPasswordV1
    ): ResponseEntity<Unit> {
        userService.updatePassword(
            newPasswordV1.actualPassword,
            newPasswordV1.newPassword,
            getUserId(),
            getSessionId(),
            Device(userAgent, appVersion)
        )

        return ResponseEntity.noContent().build()
    }

    @PreAuthorize(AUTHORIZE_SERVICE)
    override fun findById(userId: String): ResponseEntity<UserResponseV1> {
        userService.findById(userId).also { return ResponseEntity.ok(it.toV1()) }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun findByAccessToken(): ResponseEntity<UserResponseV1> {
        userService.findById(getUserId()).also { return ResponseEntity.ok(it.toV1()) }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun createAddress(addressV1: AddressV1): ResponseEntity<Unit> {
        userService.createAddress(getUserId(), addressV1.toDomain()).also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun deleteAddress(addressId: String): ResponseEntity<Unit> {
        userService.deleteAddress(getUserId(), addressId).also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun updateAddress(addressId: String, updateAddressV1: UpdateAddressV1): ResponseEntity<Unit> {
        userService.updateAddress(getUserId(), addressId, updateAddressV1.toDomain())
        return ResponseEntity.noContent().build()
    }

    override fun create(
        userAgent: String,
        appVersion: String,
        userV1: UserV1
    ): ResponseEntity<AuthenticationResponseV1> {
        userService.create(Device(userAgent, appVersion), userV1.toDomain())
            .toV1()
            .also { return ResponseEntity(it, HttpStatus.CREATED) }
    }

    override fun sendRedefinePasswordCode(redefinePasswordRequestV1: RedefinePasswordRequestV1): ResponseEntity<Unit> {
        redefinePasswordService.send(redefinePasswordRequestV1.email)

        return ResponseEntity.noContent().build()
    }

    override fun validateRedefinePasswordCode(code: String): ResponseEntity<Unit> {
        redefinePasswordService.isValidCode(code, true)

        return ResponseEntity.noContent().build()
    }

    override fun redefinePassword(redefinePasswordV1: RedefinePasswordV1): ResponseEntity<Unit> {
        userService.redefinePassword(redefinePasswordV1.code, redefinePasswordV1.newPassword)

        return ResponseEntity.noContent().build()
    }
}