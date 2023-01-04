package com.kaiqkt.services.authregistryservice.resources.mongodb

import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.PasswordSampler
import com.kaiqkt.services.authregistryservice.domain.entities.PhoneSampler
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.mongodb.client.result.UpdateResult
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class UserRepositoryCustomImplTest {
    private val mongoTemplate: MongoTemplate = mockk(relaxed = true)
    private val repository = UserRepositoryCustomImpl(mongoTemplate)

    @Test
    fun `given address to create, should create successfully`() {
        val address = AddressSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))
        val update = Update().push("addresses", address)

        every { mongoTemplate.upsert(any(), any(), User::class.java) } returns UpdateResult.acknowledged(1, null, null)

        repository.createAddress(userId, address)

        verify { mongoTemplate.upsert(query, update, User::class.java) }
    }

    @Test
    fun `given a address to delete, should delete successfully`() {
        val addressId = ULID.random()
        val userId = ULID.random()

        val update = Update().pull("addresses", Query().addCriteria(Criteria.where("id").`is`(addressId)))

        every { mongoTemplate.updateMulti(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            1,
            null,
            null
        )

        repository.deleteAddress(userId, addressId)

        verify { mongoTemplate.updateMulti(Query(), update, User::class.java) }
    }

    @Test
    fun `given a address to update, should update successfully`() {
        val address = AddressSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))

        every { mongoTemplate.upsert(any(), any(), User::class.java) } returns UpdateResult.acknowledged(1, null, null)

        repository.updateAddress(userId, address)

        verify { mongoTemplate.upsert(query, any(), User::class.java) }
    }

    @Test
    fun `given a password to update, should update the password and the updatedAt field successfully`() {
        val newPassword = PasswordSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.upsert(any(), any(), User::class.java) } returns UpdateResult.acknowledged(1, null, null)

        repository.updatePassword(userId, newPassword)

        verify { mongoTemplate.upsert(query, any(), User::class.java) }
    }

    @Test
    fun `given a phone to update, should update the phone and the updatedAt field successfully`() {
        val phone = PhoneSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.upsert(any(), any(), User::class.java) } returns UpdateResult.acknowledged(1, null, null)

        repository.updatePhone(userId, phone)

        verify { mongoTemplate.upsert(query, any(), User::class.java) }
    }
}