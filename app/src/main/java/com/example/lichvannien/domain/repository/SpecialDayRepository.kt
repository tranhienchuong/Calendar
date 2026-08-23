package com.example.lichvannien.domain.repository

import com.example.lichvannien.domain.model.SpecialDay

interface SpecialDayRepository {
    suspend fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDay>
    suspend fun getEventsForLunarDate(month: Int, day: Int, isLeapMonth: Boolean): List<SpecialDay>
    suspend fun getEventsForSolarMonth(month: Int): List<SpecialDay>
    suspend fun getEventsForLunarMonth(month: Int): List<SpecialDay>
}
