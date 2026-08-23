package com.example.lichvannien.data.repository

import com.example.lichvannien.data.local.db.SpecialDayDao
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.repository.SpecialDayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpecialDayRepositoryImpl @Inject constructor(
    private val specialDayDao: SpecialDayDao
) : SpecialDayRepository {

    override suspend fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.getEventsForSolarDate(month, day).map { entity ->
            SpecialDay(
                name = entity.name,
                icon = entity.icon,
                solarMonth = entity.solarMonth,
                solarDay = entity.solarDay,
                lunarMonth = entity.lunarMonth,
                lunarDay = entity.lunarDay,
                isLunar = entity.isLunar,
                leapMonth = entity.leapMonth
            )
        }
    }

    override suspend fun getEventsForLunarDate(month: Int, day: Int, isLeapMonth: Boolean): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.getEventsForLunarDate(month, day, isLeapMonth).map { entity ->
            SpecialDay(
                name = entity.name,
                icon = entity.icon,
                solarMonth = entity.solarMonth,
                solarDay = entity.solarDay,
                lunarMonth = entity.lunarMonth,
                lunarDay = entity.lunarDay,
                isLunar = entity.isLunar,
                leapMonth = entity.leapMonth
            )
        }
    }

    override suspend fun getEventsForSolarMonth(month: Int): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.getEventsForSolarMonth(month).map { entity ->
            SpecialDay(
                name = entity.name,
                icon = entity.icon,
                solarMonth = entity.solarMonth,
                solarDay = entity.solarDay,
                lunarMonth = entity.lunarMonth,
                lunarDay = entity.lunarDay,
                isLunar = entity.isLunar,
                leapMonth = entity.leapMonth
            )
        }
    }

    override suspend fun getEventsForLunarMonth(month: Int): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.getEventsForLunarMonth(month).map { entity ->
            SpecialDay(
                name = entity.name,
                icon = entity.icon,
                solarMonth = entity.solarMonth,
                solarDay = entity.solarDay,
                lunarMonth = entity.lunarMonth,
                lunarDay = entity.lunarDay,
                isLunar = entity.isLunar,
                leapMonth = entity.leapMonth
            )
        }
    }
}
