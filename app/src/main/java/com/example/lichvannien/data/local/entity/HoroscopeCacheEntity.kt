package com.example.lichvannien.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "horoscope_cache",
    primaryKeys = ["sign", "dayType"]
)
data class HoroscopeCacheEntity(
    val sign: String,
    val dayType: String,
    val jsonData: String,
    val timestamp: Long
)
