package com.kaiqkt.services.authregistryservice.config

import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.process.config.RuntimeConfig
import de.flapdoodle.embed.process.runtime.Network
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.io.IOException

@TestConfiguration
@AutoConfigureBefore(EmbeddedMongoAutoConfiguration::class)
class EmbeddedMongoDBConfig {

    @Bean
    @ConditionalOnMissingBean
    @Throws(IOException::class)
    fun start(): MongodConfig {
        val command: Command = Command.MongoD
        val runtimeConfig: RuntimeConfig = Defaults.runtimeConfigFor(command)
            .artifactStore(
                Defaults.extractedArtifactStoreFor(command)
                    .withDownloadConfig(Defaults.downloadConfigFor(command).build())
            ).build()

        MongodStarter.getInstance(runtimeConfig)

        val ip = "localhost"
        val port = 27017
        return MongodConfig
            .builder()
            .version(Version.Main.V4_0)
            .net(Net(ip, port, Network.localhostIsIPv6()))
            .build()
    }
}