package com.example.lichvannien.domain.usecase

import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.UserBirthday
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBirthdayUseCaseTest {

    private val userPreferences: UserPreferences = mockk()
    private val useCase = GetBirthdayUseCase(userPreferences)

    @Test
    fun invoke_returnsBirthdayFlowFromUserPreferences() = runTest {
        val expectedBirthday = UserBirthday(day = 20, month = 11, year = 1998)
        every { userPreferences.birthdayFlow } returns flowOf(expectedBirthday)

        val result = useCase().first()

        assertThat(result).isEqualTo(expectedBirthday)
        assertThat(result.day).isEqualTo(20)
        assertThat(result.month).isEqualTo(11)
        assertThat(result.year).isEqualTo(1998)
        assertThat(result.isConfigured).isTrue()
    }
}
