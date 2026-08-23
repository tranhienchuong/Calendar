package com.example.lichvannien.domain.repository

import com.example.lichvannien.domain.model.Horoscope

interface HoroscopeRepository {
    suspend fun getHoroscope(sign: String, dayType: String): Result<Horoscope>
}
