package io.commerce.accountservice.profile

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProfileSyncRepositoryImpl(
    @Qualifier("reactiveMongoTemplate")
    private val ops: ReactiveMongoTemplate
) : ProfileSyncRepository {
    /**
     * profile collection 내에 customerId가 존재하는 경우, 소셜 리스트 업데이트
     * profile collection 내에 customerId가 없는 경우, 프로필 생성
     *
     * @param profile 프로필 데이터
     */
    override suspend fun upsertIdentityProviders(profile: Profile): Profile {
        val now = Instant.now()
        val criteria = where(Profile::customerId).isEqualTo(profile.customerId)
        val query = Query.query(criteria)
        val update = Update()
            .set(Profile::identityProviders.name, profile.identityProviders)
            .set(Profile::updatedAt.name, now)
            .setOnInsert(Profile::enabled.name, profile.enabled)
            .setOnInsert(Profile::email.name, profile.email)
            .setOnInsert(Profile::emailVerified.name, profile.emailVerified)
            .setOnInsert(Profile::name.name, profile.name)
            .setOnInsert(Profile::phoneNumber.name, profile.phoneNumber)
            .setOnInsert(Profile::phoneNumberVerified.name, profile.phoneNumberVerified)
            .setOnInsert(Profile::birthday.name, profile.birthday)
            .setOnInsert(Profile::agreement.name, profile.agreement)
            .setOnInsert(Profile::orderCount.name, 0)
            .setOnInsert(Profile::createdAt.name, now)
        val option = FindAndModifyOptions().upsert(true).returnNew(true)
        return ops.findAndModify(query, update, option, Profile::class.java).awaitSingle()
    }
}
