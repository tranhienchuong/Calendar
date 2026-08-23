package com.example.lichvannien.domain.model

data class SpecialDay(
    val name: String,
    val icon: String?,
    val solarMonth: Int? = null,
    val solarDay: Int? = null,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
    val isLunar: Boolean = false,
    val leapMonth: Boolean = false
)
