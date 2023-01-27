package io.commerce.accountservice.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@Configuration
@EnableReactiveMongoAuditing
class ReactiveMongoClientConfig(
    private val properties: MongoProperties
) : AbstractReactiveMongoConfiguration() {
    /**
     * Reactive Mongo Transaction Manager 활성화
     *
     * Connection string에 `replicaSet` 설정이 존재하고, 활성화 되어 있어야 @Transactional 사용 가능
     *
     * [Reactive Transactions](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions.reactive)
     *
     * [Transactions with ReactiveMongoTransactionManager](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions.reactive-tx-manager)
     */
    @Bean
    fun reactiveMongoTransactionManager(
        reactiveMongoDatabaseFactory: ReactiveMongoDatabaseFactory
    ): ReactiveMongoTransactionManager =
        ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory)

    override fun getDatabaseName(): String = properties.database

    @Bean
    override fun reactiveMongoClient(): MongoClient = super.reactiveMongoClient()

    override fun configureClientSettings(builder: MongoClientSettings.Builder) {
        builder.applyConnectionString(ConnectionString(properties.uri))
    }

    override fun autoIndexCreation(): Boolean = properties.isAutoIndexCreation
}
