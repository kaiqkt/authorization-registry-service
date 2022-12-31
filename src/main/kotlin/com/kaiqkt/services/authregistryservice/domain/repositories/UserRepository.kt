package com.kaiqkt.services.authregistryservice.domain.repositories

import com.kaiqkt.services.authregistryservice.domain.entities.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: MongoRepository<User, String> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}