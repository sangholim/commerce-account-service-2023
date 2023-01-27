package io.commerce.accountservice.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import io.mongock.driver.mongodb.reactive.util.MongoSubscriberSync
import org.bson.Document
import org.springframework.core.io.ClassPathResource

/**
 * Profile orderCount 초기화 값 추가 '0'
 */
@ChangeUnit(id = "profile-change-00001", order = "00001")
class ProfilerChange00001 {
    private val resourcePath = "migration/profile-change-00001.json"

    @Execution
    fun execution(mapper: ObjectMapper, mongoDatabase: MongoDatabase) {
        val query = mapper.readValue<JsonQuery>(ClassPathResource(resourcePath).file)
        val subscriber = MongoSubscriberSync<UpdateResult>()

        mongoDatabase.getCollection("profile")
            .updateMany(query.filter, query.update)
            .subscribe(subscriber)

        subscriber.await()
    }

    @RollbackExecution
    fun rollback() = Unit

    data class JsonQuery(
        val filter: Document,
        val update: List<Document>
    )
}
