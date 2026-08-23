package com.example.lichvannien.domain.model

data class DayDetail(
    val solarDate: SolarDate,
    val lunarDate: LunarDate,
    val auspicious: AuspiciousResult,
    val specialDays: List<SpecialDay>
)
