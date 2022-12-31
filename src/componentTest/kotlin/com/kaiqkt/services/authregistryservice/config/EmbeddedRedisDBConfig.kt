package com.kaiqkt.services.authregistryservice.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import redis.embedded.RedisServer
import javax.annotation.PostConstruct


@TestConfiguration
@EnableConfigurationProperties(RedisProperties::class)
class EmbeddedRedisDBConfig(private val properties: RedisProperties) {

    var redisServer: RedisServer = RedisServer.builder()
        .port(properties.port)
        .setting("requirepass ${properties.password}")
        .setting("maxmemory 128M")
        .build()

    @PostConstruct
    fun postConstruct() {
        redisServer.start()
    }

    fun destroy() {
        redisServer.stop()
    }

}
