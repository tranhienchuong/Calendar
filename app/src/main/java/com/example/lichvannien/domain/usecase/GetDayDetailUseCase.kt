package com.example.lichvannien.domain.usecase

import com.example.lichvannien.domain.model.DayDetail
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetDayDetailUseCase @Inject constructor(
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val specialDayRepository: SpecialDayRepository
) {
    suspend operator fun invoke(year: Int, month: Int, day: Int): DayDetail = withContext(Dispatchers.Default) {
        val solarDate = SolarDate(year, month, day)
        val lunarDate = lunarConverter.solarToLunar(year, month, day)
        val auspicious = auspiciousCalculator.calculate(lunarDate)
        
        // Gọi repository để lấy sự kiện (DB operation nên chạy song song hoặc tuần tự trên IO)
        val solarEvents = specialDayRepository.getEventsForSolarDate(month, day)
        val lunarEvents = specialDayRepository.getEventsForLunarDate(lunarDate.month, lunarDate.day, lunarDate.isLeapMonth)
        val specialDays = solarEvents + lunarEvents

        DayDetail(
            solarDate = solarDate,
            lunarDate = lunarDate,
            auspicious = auspicious,
            specialDays = specialDays
        )
    }
}
