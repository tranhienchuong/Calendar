package com.example.lichvannien.domain.usecase

import com.example.lichvannien.domain.model.AuspiciousResult
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetDayDetailUseCaseTest {

    private val lunarConverter: LunarConverter = mockk()
    private val auspiciousCalculator: AuspiciousCalculator = mockk()
    private val specialDayRepository: SpecialDayRepository = mockk()
    private val useCase = GetDayDetailUseCase(lunarConverter, auspiciousCalculator, specialDayRepository)

    @Test
    fun invoke_returnsCombinedDayDetail() = runTest {
        // Arrange
        val year = 2023
        val month = 1
        val day = 22

        val mockLunar = LunarDate(
            year = 2023,
            month = 1,
            day = 1,
            isLeapMonth = false,
            canChiDay = "Canh Thìn",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val mockAuspicious = AuspiciousResult(
            isHoangDao = true,
            truc = "Bình",
            gioHoangDao = listOf("Dần (3h-5h)")
        )
        val mockSolarEvents = listOf(SpecialDay("Sự kiện dương", "☀️"))
        val mockLunarEvents = listOf(SpecialDay("Tết Nguyên Đán", "🧧"))

        coEvery { lunarConverter.solarToLunar(year, month, day) } returns mockLunar
        coEvery { auspiciousCalculator.calculate(mockLunar) } returns mockAuspicious
        coEvery { specialDayRepository.getEventsForSolarDate(month, day) } returns mockSolarEvents
        coEvery { specialDayRepository.getEventsForLunarDate(mockLunar.month, mockLunar.day, mockLunar.isLeapMonth) } returns mockLunarEvents

        // Act
        val result = useCase(year, month, day)

        // Assert
        assertThat(result.solarDate).isEqualTo(SolarDate(year, month, day))
        assertThat(result.lunarDate).isEqualTo(mockLunar)
        assertThat(result.auspicious).isEqualTo(mockAuspicious)
        assertThat(result.specialDays).hasSize(2)
        assertThat(result.specialDays).containsExactlyElementsIn(mockSolarEvents + mockLunarEvents)

        coVerify(exactly = 1) { lunarConverter.solarToLunar(year, month, day) }
        coVerify(exactly = 1) { auspiciousCalculator.calculate(mockLunar) }
        coVerify(exactly = 1) { specialDayRepository.getEventsForSolarDate(month, day) }
        coVerify(exactly = 1) { specialDayRepository.getEventsForLunarDate(mockLunar.month, mockLunar.day, mockLunar.isLeapMonth) }
    }
}
