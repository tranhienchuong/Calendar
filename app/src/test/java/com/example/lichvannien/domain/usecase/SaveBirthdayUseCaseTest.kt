package com.example.lichvannien.domain.usecase

import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.UserBirthday
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveBirthdayUseCaseTest {

    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val useCase = SaveBirthdayUseCase(userPreferences)

    @Test
    fun invoke_withModel_callsUserPreferencesSaveBirthday() = runTest {
        val birthday = UserBirthday(day = 10, month = 5, year = 2000)

        useCase(birthday)

        coVerify(exactly = 1) { userPreferences.saveBirthday(10, 5, 2000) }
    }

    @Test
    fun invoke_withParams_callsUserPreferencesSaveBirthday() = runTest {
        useCase(1, 1, 1990)

        coVerify(exactly = 1) { userPreferences.saveBirthday(1, 1, 1990) }
    }
}
