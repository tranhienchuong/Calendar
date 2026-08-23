package com.example.lichvannien.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lichvannien.data.local.entity.HoroscopeCacheEntity

@Dao
interface HoroscopeCacheDao {

    @Query("SELECT * FROM horoscope_cache WHERE sign = :sign AND dayType = :dayType")
    fun getCache(sign: String, dayType: String): HoroscopeCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cache: HoroscopeCacheEntity)

    @Query("DELETE FROM horoscope_cache WHERE sign = :sign AND dayType = :dayType")
    fun deleteCache(sign: String, dayType: String)
}
