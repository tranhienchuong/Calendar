package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.AuspiciousResult
import com.example.lichvannien.domain.model.LunarDate

object AuspiciousCalculator {

    val TRUC = listOf("Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thâu", "Khai", "Bế")

    val HOANG_DAO_MAP = mapOf(
        1 to setOf(1, 2, 5, 6, 9, 10),   // Tháng 1: Tý, Sửu, Thìn, Tỵ, Thân, Dậu
        2 to setOf(3, 4, 7, 8, 11, 12),  // Tháng 2: Dần, Mão, Ngọ, Mùi, Tuất, Hợi
        3 to setOf(5, 6, 9, 10, 1, 2),   // Tháng 3: Thìn, Tỵ, Thân, Dậu, Tý, Sửu
        4 to setOf(7, 8, 11, 12, 3, 4),  // Tháng 4: Ngọ, Mùi, Tuất, Hợi, Dần, Mão
        5 to setOf(9, 10, 1, 2, 5, 6),   // Tháng 5: Thân, Dậu, Tý, Sửu, Thìn, Tỵ
        6 to setOf(11, 12, 3, 4, 7, 8),  // Tháng 6: Tuất, Hợi, Dần, Mão, Ngọ, Mùi
        7 to setOf(1, 2, 5, 6, 9, 10),   // Tháng 7: Tý, Sửu, Thìn, Tỵ, Thân, Dậu
        8 to setOf(3, 4, 7, 8, 11, 12),  // Tháng 8: Dần, Mão, Ngọ, Mùi, Tuất, Hợi
        9 to setOf(5, 6, 9, 10, 1, 2),   // Tháng 9: Thìn, Tỵ, Thân, Dậu, Tý, Sửu
        10 to setOf(7, 8, 11, 12, 3, 4), // Tháng 10: Ngọ, Mùi, Tuất, Hợi, Dần, Mão
        11 to setOf(9, 10, 1, 2, 5, 6),  // Tháng 11: Thân, Dậu, Tý, Sửu, Thìn, Tỵ
        12 to setOf(11, 12, 3, 4, 7, 8)  // Tháng 12: Tuất, Hợi, Dần, Mão, Ngọ, Mùi
    )

    // Bảng giờ hoàng đạo chính xác theo spec.md mục 4.3 (ngày Tý, Sửu, Dần có 5 chi giờ hoàng đạo)
    val GIO_HOANG_DAO_MAP = mapOf(
        "Tý" to setOf("Sửu", "Thìn", "Tỵ", "Mùi", "Tuất"),
        "Sửu" to setOf("Tý", "Dần", "Mão", "Ngọ", "Thân"),
        "Dần" to setOf("Sửu", "Thìn", "Tỵ", "Mùi", "Dậu"),
        "Mão" to setOf("Tý", "Dần", "Mão", "Ngọ", "Thân", "Tuất"),
        "Thìn" to setOf("Dần", "Thìn", "Tỵ", "Thân", "Dậu", "Hợi"),
        "Tỵ" to setOf("Sửu", "Thìn", "Ngọ", "Mùi", "Tuất", "Hợi"),
        "Ngọ" to setOf("Tý", "Sửu", "Thìn", "Tỵ", "Mùi", "Tuất"),
        "Mùi" to setOf("Dần", "Mão", "Tỵ", "Thân", "Tuất", "Hợi"),
        "Thân" to setOf("Tý", "Dần", "Mão", "Ngọ", "Thân", "Dậu"),
        "Dậu" to setOf("Sửu", "Thìn", "Tỵ", "Mùi", "Dậu", "Hợi"),
        "Tuất" to setOf("Dần", "Mão", "Ngọ", "Thân", "Tuất", "Hợi"),
        "Hợi" to setOf("Tý", "Sửu", "Thìn", "Ngọ", "Mùi", "Dậu")
    )

    val GIO_DUONG_MAP = mapOf(
        "Tý" to "23h-1h",
        "Sửu" to "1h-3h",
        "Dần" to "3h-5h",
        "Mão" to "5h-7h",
        "Thìn" to "7h-9h",
        "Tỵ" to "9h-11h",
        "Ngọ" to "11h-13h",
        "Mùi" to "13h-15h",
        "Thân" to "15h-17h",
        "Dậu" to "17h-19h",
        "Tuất" to "19h-21h",
        "Hợi" to "21h-23h"
    )

    fun calculate(lunarDate: LunarDate): AuspiciousResult {
        // 1. Lấy chi ngày từ canChiDay
        val dayChi = lunarDate.canChiDay.split(" ").lastOrNull() ?: ""
        val chiIndex = LunarConverter.CHI.indexOf(dayChi)
        val chiNgay = if (chiIndex != -1) chiIndex + 1 else 1 // 1-indexed

        // 2. Tính Trực
        val chiThangRaw = (lunarDate.month + 1) % 12
        val chiThang = if (chiThangRaw == 0) 12 else chiThangRaw
        val offset = (chiNgay - chiThang + 12) % 12
        val truc = TRUC[offset]

        // 3. Xác định Hoàng Đạo / Hắc Đạo
        val hoangDaoSet = HOANG_DAO_MAP[lunarDate.month] ?: emptySet()
        val isHoangDao = hoangDaoSet.contains(chiNgay)

        // 4. Tính Giờ Hoàng Đạo (sắp xếp theo thứ tự thời gian tự nhiên)
        val gioHoangDaoChiSet = GIO_HOANG_DAO_MAP[dayChi] ?: emptySet()
        val sortedGioHoangDaoChi = gioHoangDaoChiSet.sortedBy { LunarConverter.CHI.indexOf(it) }
        val gioHoangDao = sortedGioHoangDaoChi.map { chiGio ->
            val gioDuong = GIO_DUONG_MAP[chiGio] ?: ""
            "$chiGio ($gioDuong)"
        }

        return AuspiciousResult(
            isHoangDao = isHoangDao,
            truc = truc,
            gioHoangDao = gioHoangDao
        )
    }
}
