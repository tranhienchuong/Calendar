package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.ZodiacSign

object ZodiacHelper {

    private val zodiacSigns = listOf(
        ZodiacSign("Bạch Dương", "Aries", "♈", 21, 3, 19, 4),
        ZodiacSign("Kim Ngưu", "Taurus", "♉", 20, 4, 20, 5),
        ZodiacSign("Song Tử", "Gemini", "♊", 21, 5, 20, 6),
        ZodiacSign("Cự Giải", "Cancer", "♋", 21, 6, 22, 7),
        ZodiacSign("Sư Tử", "Leo", "♌", 23, 7, 22, 8),
        ZodiacSign("Xử Nữ", "Virgo", "♍", 23, 8, 22, 9),
        ZodiacSign("Thiên Bình", "Libra", "♎", 23, 9, 22, 10),
        ZodiacSign("Bọ Cạp", "Scorpio", "♏", 23, 10, 21, 11),
        ZodiacSign("Nhân Mã", "Sagittarius", "♐", 22, 11, 21, 12),
        ZodiacSign("Ma Kết", "Capricorn", "♑", 22, 12, 19, 1),
        ZodiacSign("Bảo Bình", "Aquarius", "♒", 20, 1, 18, 2),
        ZodiacSign("Song Ngư", "Pisces", "♓", 19, 2, 20, 3)
    )

    fun isValidDate(day: Int, month: Int): Boolean {
        if (month !in 1..12) return false
        val maxDays = when (month) {
            2 -> 29 // Hỗ trợ năm nhuận 29 ngày để an toàn
            4, 6, 9, 11 -> 30
            else -> 31
        }
        return day in 1..maxDays
    }

    fun getZodiacSign(day: Int, month: Int): ZodiacSign? {
        if (!isValidDate(day, month)) return null
        
        return zodiacSigns.firstOrNull { sign ->
            (month == sign.startMonth && day >= sign.startDay) ||
            (month == sign.endMonth && day <= sign.endDay)
        }
    }
}
