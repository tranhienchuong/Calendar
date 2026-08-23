package com.example.lichvannien.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "special_days",
    indices = [
        Index(value = ["solarMonth", "solarDay"]),
        Index(value = ["lunarMonth", "lunarDay"])
    ]
)
data class SpecialDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val solarMonth: Int?,
    val solarDay: Int?,
    val lunarMonth: Int?,
    val lunarDay: Int?,
    val isLunar: Boolean,
    val leapMonth: Boolean = false,
    val icon: String?
)
