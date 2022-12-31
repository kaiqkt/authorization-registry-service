package com.kaiqkt.services.authregistryservice.application.security

import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.commons.security.auth.token.CustomAuthentication
import io.azam.ulidj.ULID
import org.springframework.security.core.authority.SimpleGrantedAuthority

object CustomAuthenticationSampler {
    fun sample() = CustomAuthentication(ULID.random()).apply {
        isAuthenticated = true
        authorities.add(SimpleGrantedAuthority(ROLE_USER))
        id = "01GFPPTXKZ8ZJRG8MF701M0W99"
        sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"
        refreshToken = "031231amdsfakKKAy"
    }

    fun sampleExpired() = CustomAuthentication(ULID.random()).apply {
        isAuthenticated = true
        isExpired = true
        authorities.add(SimpleGrantedAuthority(ROLE_USER))
        id = "01GFPPTXKZ8ZJRG8MF701M0W99"
        sessionId = "01GFPPTXKZ8ZJRG8MF701M0W88"
        refreshToken = "031231amdsfakKKAy"
    }
}