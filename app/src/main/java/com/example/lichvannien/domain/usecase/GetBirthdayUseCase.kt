package com.example.lichvannien.domain.usecase

import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.UserBirthday
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBirthdayUseCase @Inject constructor(
    private val userPreferences: UserPreferences
) {
    operator fun invoke(): Flow<UserBirthday> = userPreferences.birthdayFlow
}
