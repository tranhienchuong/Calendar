package com.example.lichvannien.domain.model

data class CalendarDay(
    val solarDate: SolarDate,
    val lunarDate: LunarDate?,
    val isHoangDao: Boolean,
    val hasSpecialEvent: Boolean,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)
