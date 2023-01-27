package io.commerce.accountservice.profile

import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
interface ProfileSyncRepository {
    suspend fun upsertIdentityProviders(@Valid profile: Profile): Profile
}
