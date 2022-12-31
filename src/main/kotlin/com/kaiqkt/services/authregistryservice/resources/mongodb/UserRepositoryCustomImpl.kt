package com.kaiqkt.services.authregistryservice.resources.mongodb

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.Phone
import com.kaiqkt.services.authregistryservice.domain.entities.UpdateAddress
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


    override fun updateAddress(userId: String, addressId: String, updateAddress: UpdateAddress) {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(addressId))
        
        val update = Update().apply {
            if (updateAddress.zipCode != null) this.set("addresses.$.zipCode", updateAddress.zipCode)
            if (updateAddress.street != null) this.set("addresses.$.street", updateAddress.street)
            if (updateAddress.district != null) this.set("addresses.$.district", updateAddress.district)
            if (updateAddress.complement != null) this.set("addresses.$.complement", updateAddress.complement)
            if (updateAddress.number != null) this.set("addresses.$.number", updateAddress.number)
            if (updateAddress.city != null) this.set("addresses.$.city", updateAddress.city)
            if (updateAddress.state != null) this.set("addresses.$.state", updateAddress.state)
            if (updateAddress.country != null) this.set("addresses.$.country", updateAddress.country)
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