package com.example.lichvannien.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lichvannien.data.local.entity.SpecialDayEntity

@Dao
interface SpecialDayDao {
    @Query("SELECT * FROM special_days WHERE isLunar = 0 AND solarMonth = :month AND solarDay = :day")
    fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDayEntity>

    @Query("SELECT * FROM special_days WHERE isLunar = 0 AND solarMonth = :month")
    fun getEventsForSolarMonth(month: Int): List<SpecialDayEntity>

    @Query("SELECT * FROM special_days WHERE isLunar = 1 AND lunarMonth = :month")
    fun getEventsForLunarMonth(month: Int): List<SpecialDayEntity>

    @Query("SELECT * FROM special_days WHERE isLunar = 1 AND lunarMonth = :month AND lunarDay = :day AND leapMonth = :isLeap")
    fun getEventsForLunarDate(month: Int, day: Int, isLeap: Boolean): List<SpecialDayEntity>

    @Query("SELECT COUNT(*) FROM special_days")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(events: List<SpecialDayEntity>): List<Long>
}
