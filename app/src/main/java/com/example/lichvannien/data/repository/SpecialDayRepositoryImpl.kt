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

    private val solarDateCache = java.util.concurrent.ConcurrentHashMap<Pair<Int, Int>, List<SpecialDay>>()
    private val lunarDateCache = java.util.concurrent.ConcurrentHashMap<Triple<Int, Int, Boolean>, List<SpecialDay>>()
    private val solarMonthCache = java.util.concurrent.ConcurrentHashMap<Int, List<SpecialDay>>()
    private val lunarMonthCache = java.util.concurrent.ConcurrentHashMap<Int, List<SpecialDay>>()

    override suspend fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDay> {
        solarDateCache[Pair(month, day)]?.let { return it }
        return withContext(Dispatchers.IO) {
            val list = specialDayDao.getEventsForSolarDate(month, day).map { entity ->
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
            solarDateCache[Pair(month, day)] = list
            list
        }
    }

    override suspend fun getEventsForLunarDate(month: Int, day: Int, isLeapMonth: Boolean): List<SpecialDay> {
        val key = Triple(month, day, isLeapMonth)
        lunarDateCache[key]?.let { return it }
        return withContext(Dispatchers.IO) {
            val list = specialDayDao.getEventsForLunarDate(month, day, isLeapMonth).map { entity ->
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
            lunarDateCache[key] = list
            list
        }
    }

    override suspend fun getEventsForSolarMonth(month: Int): List<SpecialDay> {
        solarMonthCache[month]?.let { return it }
        return withContext(Dispatchers.IO) {
            val list = specialDayDao.getEventsForSolarMonth(month).map { entity ->
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
            solarMonthCache[month] = list
            list
        }
    }

    override suspend fun getEventsForLunarMonth(month: Int): List<SpecialDay> {
        lunarMonthCache[month]?.let { return it }
        return withContext(Dispatchers.IO) {
            val list = specialDayDao.getEventsForLunarMonth(month).map { entity ->
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
            lunarMonthCache[month] = list
            list
        }
    }

    override suspend fun searchSpecialDays(query: String): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.searchSpecialDays(query).map { entity ->
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

    override suspend fun getAllSpecialDays(): List<SpecialDay> = withContext(Dispatchers.IO) {
        specialDayDao.getAllSpecialDays().map { entity ->
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
