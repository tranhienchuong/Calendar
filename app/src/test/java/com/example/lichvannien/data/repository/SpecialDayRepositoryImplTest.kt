package com.example.lichvannien.data.repository

import com.example.lichvannien.data.local.db.SpecialDayDao
import com.example.lichvannien.data.local.entity.SpecialDayEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpecialDayRepositoryImplTest {

    private val specialDayDao: SpecialDayDao = mockk()
    private val repository = SpecialDayRepositoryImpl(specialDayDao)

    @Test
    fun getEventsForSolarDate_returnsMappedSpecialDays() = runTest {
        // Arrange
        val month = 1
        val day = 1
        val entities = listOf(
            SpecialDayEntity(
                id = 1,
                name = "Tết Dương lịch",
                solarMonth = 1,
                solarDay = 1,
                lunarMonth = null,
                lunarDay = null,
                isLunar = false,
                icon = "🎉"
            )
        )
        coEvery { specialDayDao.getEventsForSolarDate(month, day) } returns entities

        // Act
        val result = repository.getEventsForSolarDate(month, day)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Tết Dương lịch")
        assertThat(result[0].icon).isEqualTo("🎉")
        coVerify(exactly = 1) { specialDayDao.getEventsForSolarDate(month, day) }
    }

    @Test
    fun getEventsForLunarDate_returnsMappedSpecialDays_whenNotLeap() = runTest {
        // Arrange
        val month = 1
        val day = 1
        val isLeap = false
        val entities = listOf(
            SpecialDayEntity(
                id = 2,
                name = "Tết Nguyên Đán",
                solarMonth = null,
                solarDay = null,
                lunarMonth = 1,
                lunarDay = 1,
                isLunar = true,
                leapMonth = false,
                icon = "🧧"
            )
        )
        coEvery { specialDayDao.getEventsForLunarDate(month, day, isLeap) } returns entities

        // Act
        val result = repository.getEventsForLunarDate(month, day, isLeap)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Tết Nguyên Đán")
        assertThat(result[0].icon).isEqualTo("🧧")
        coVerify(exactly = 1) { specialDayDao.getEventsForLunarDate(month, day, isLeap) }
    }

    @Test
    fun getEventsForLunarDate_returnsMappedSpecialDays_whenLeap() = runTest {
        // Arrange
        val month = 4
        val day = 15
        val isLeap = true
        val entities = listOf(
            SpecialDayEntity(
                id = 3,
                name = "Sự kiện tháng nhuận",
                solarMonth = null,
                solarDay = null,
                lunarMonth = 4,
                lunarDay = 15,
                isLunar = true,
                leapMonth = true,
                icon = "🌸"
            )
        )
        coEvery { specialDayDao.getEventsForLunarDate(month, day, isLeap) } returns entities

        // Act
        val result = repository.getEventsForLunarDate(month, day, isLeap)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Sự kiện tháng nhuận")
        assertThat(result[0].icon).isEqualTo("🌸")
        coVerify(exactly = 1) { specialDayDao.getEventsForLunarDate(month, day, isLeap) }
    }
}
