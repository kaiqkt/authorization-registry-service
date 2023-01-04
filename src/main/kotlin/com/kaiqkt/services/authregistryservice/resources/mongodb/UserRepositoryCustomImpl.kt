package com.kaiqkt.services.authregistryservice.resources.mongodb

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepositoryCustom
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserRepositoryCustomImpl(private val mongoTemplate: MongoTemplate) : UserRepositoryCustom {
    override fun createAddress(userId: String, address: Address) {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId))
        val update = Update().push("addresses", address)

        mongoTemplate.upsert(query, update, User::class.java)
    }

    override fun deleteAddress(userId: String, addressId: String) {
        mongoTemplate.updateMulti(
            Query(),
            Update().pull("addresses", Query().addCriteria(Criteria.where("id").`is`(addressId))),
            User::class.java
        )
    }


    override fun updateAddress(userId: String, address: Address) {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))
        
        val update = Update().apply {
            this.set("addresses.$.zipCode", address.zipCode)
            this.set("addresses.$.street", address.street)
            this.set("addresses.$.district", address.district)
            this.set("addresses.$.complement", address.complement)
            this.set("addresses.$.number", address.number)
            this.set("addresses.$.city", address.city)
            this.set("addresses.$.state", address.state)
            this.set("addresses.$.country", address.country)
            this.set("updatedAt", LocalDateTime.now())
        }

        mongoTemplate.upsert(query, update, User::class.java)
    }

    override fun updatePassword(userId: String, newPassword: Password) {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId))
        val update = Update().apply {
            this.set("password", newPassword)
            this.set("updatedAt", LocalDateTime.now())
        }

        mongoTemplate.upsert(query, update, User::class.java)
    }

    override fun updatePhone(userId: String, phone: Phone) {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId))
        val update = Update().apply {
            this.set("phone", phone)
            this.set("updatedAt", LocalDateTime.now())
        }

        mongoTemplate.upsert(query, update, User::class.java)
    }
}