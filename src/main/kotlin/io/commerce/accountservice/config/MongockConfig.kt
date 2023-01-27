package io.commerce.accountservice.config

import com.mongodb.reactivestreams.client.MongoClient
import io.mongock.driver.mongodb.reactive.driver.MongoReactiveDriver
import io.mongock.runner.springboot.MongockSpringboot
import io.mongock.runner.springboot.base.MongockInitializingBeanRunner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongockConfig {
    @Bean
    fun mongockInitializer(
        context: ApplicationContext,
        mongoClient: MongoClient,
        @Value("\${spring.data.mongodb.database}")
        database: String,
        @Value("\${mongock.enabled}")
        mongockEnabled: Boolean
    ): MongockInitializingBeanRunner =
        MongockSpringboot.builder()
            .setDriver(MongoReactiveDriver.withDefaultLock(mongoClient, database))
            .addMigrationScanPackage("io.commerce.accountservice.database")
            .setSpringContext(context)
            .setTransactionEnabled(true)
            .setEnabled(mongockEnabled)
            .buildInitializingBeanRunner()
}
