package com.example.lichvannien.domain.usecase

import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.UserBirthday
import javax.inject.Inject

class SaveBirthdayUseCase @Inject constructor(
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke(birthday: UserBirthday) {
        userPreferences.saveBirthday(birthday.day, birthday.month, birthday.year)
    }

    suspend operator fun invoke(day: Int, month: Int, year: Int) {
        userPreferences.saveBirthday(day, month, year)
    }
}
