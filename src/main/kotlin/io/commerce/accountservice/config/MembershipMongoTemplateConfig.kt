package io.commerce.accountservice.config

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = [
        "io.commerce.accountservice.membership",
        "io.commerce.accountservice.storeCreditAccount"
    ],
    reactiveMongoTemplateRef = "membershipTemplate"
)
class MembershipMongoTemplateConfig(
    private val mongoClient: MongoClient
) {
    @Bean
    fun membershipTemplate(): ReactiveMongoTemplate = ReactiveMongoTemplate(
        SimpleReactiveMongoDatabaseFactory(mongoClient, "membership-db")
    )
}
