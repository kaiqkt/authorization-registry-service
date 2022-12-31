package com.kaiqkt.services.authregistryservice.domain.repositories

interface RedefinePasswordRepository{
    fun findByCode(code: String): String?
    fun save(code: String, userId: String)
    fun delete(code: String)
}
