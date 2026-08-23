package com.example.lichvannien.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HoroscopeResponse(
    val current_date: String,
    val description: String,
    val compatibility: String,
    val mood: String,
    val color: String,
    val lucky_number: String,
    val lucky_time: String
)
