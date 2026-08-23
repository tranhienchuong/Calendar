package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LunarConverterTest {

    @Test
    fun testSolarToLunar_case1_newYearSolar2023() {
        // 01/01/2023 dương -> 10/12/2022 âm (Nhâm Dần)
        val lunar = LunarConverter.solarToLunar(2023, 1, 1)
        assertThat(lunar.day).isEqualTo(10)
        assertThat(lunar.month).isEqualTo(12)
        assertThat(lunar.year).isEqualTo(2022)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Kỷ Mùi")
        assertThat(lunar.canChiMonth).isEqualTo("Quý Sửu")
        assertThat(lunar.canChiYear).isEqualTo("Nhâm Dần")
    }

    @Test
    fun testSolarToLunar_case2_tetQuyMao2023() {
        // 22/01/2023 dương -> Mùng 1 Tết Quý Mão (01/01/2023 âm)
        val lunar = LunarConverter.solarToLunar(2023, 1, 22)
        assertThat(lunar.day).isEqualTo(1)
        assertThat(lunar.month).isEqualTo(1)
        assertThat(lunar.year).isEqualTo(2023)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Canh Thìn")
        assertThat(lunar.canChiMonth).isEqualTo("Giáp Dần")
        assertThat(lunar.canChiYear).isEqualTo("Quý Mão")
    }

    @Test
    fun testSolarToLunar_case3_vuLan2023() {
        // 30/08/2023 dương -> Rằm tháng 7 (15/07/2023 âm)
        val lunar = LunarConverter.solarToLunar(2023, 8, 30)
        assertThat(lunar.day).isEqualTo(15)
        assertThat(lunar.month).isEqualTo(7)
        assertThat(lunar.year).isEqualTo(2023)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Canh Thân")
        assertThat(lunar.canChiMonth).isEqualTo("Canh Thân")
        assertThat(lunar.canChiYear).isEqualTo("Quý Mão")
    }

    @Test
    fun testSolarToLunar_case4_trungThu2023() {
        // 29/09/2023 dương -> Rằm tháng 8 (15/08/2023 âm)
        val lunar = LunarConverter.solarToLunar(2023, 9, 29)
        assertThat(lunar.day).isEqualTo(15)
        assertThat(lunar.month).isEqualTo(8)
        assertThat(lunar.year).isEqualTo(2023)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Canh Dần")
        assertThat(lunar.canChiMonth).isEqualTo("Tân Dậu")
        assertThat(lunar.canChiYear).isEqualTo("Quý Mão")
    }

    @Test
    fun testSolarToLunar_case5_earlyFeb2026() {
        // 05/02/2026 dương -> 18/12/2025 âm (Ất Tỵ)
        val lunar = LunarConverter.solarToLunar(2026, 2, 5)
        assertThat(lunar.day).isEqualTo(18)
        assertThat(lunar.month).isEqualTo(12)
        assertThat(lunar.year).isEqualTo(2025)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Canh Tuất")
        assertThat(lunar.canChiMonth).isEqualTo("Kỷ Sửu")
        assertThat(lunar.canChiYear).isEqualTo("Ất Tỵ")
    }

    @Test
    fun testSolarToLunar_case6_tetBinhNgo2026() {
        // 17/02/2026 dương -> Mùng 1 Tết Bính Ngọ (01/01/2026 âm)
        val lunar = LunarConverter.solarToLunar(2026, 2, 17)
        assertThat(lunar.day).isEqualTo(1)
        assertThat(lunar.month).isEqualTo(1)
        assertThat(lunar.year).isEqualTo(2026)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Nhâm Tuất")
        assertThat(lunar.canChiMonth).isEqualTo("Canh Dần")
        assertThat(lunar.canChiYear).isEqualTo("Bính Ngọ")
    }

    @Test
    fun testSolarToLunar_case7_leapMonthCanhTy2020() {
        // 22/03/2020 dương -> 29/02/2020 âm (năm Canh Tý)
        val lunar = LunarConverter.solarToLunar(2020, 3, 22)
        assertThat(lunar.day).isEqualTo(29)
        assertThat(lunar.month).isEqualTo(2)
        assertThat(lunar.year).isEqualTo(2020)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Giáp Tý")
        assertThat(lunar.canChiMonth).isEqualTo("Kỷ Mão")
        assertThat(lunar.canChiYear).isEqualTo("Canh Tý")
    }

    @Test
    fun testSolarToLunar_case8_earlyMarch2000() {
        // 09/03/2000 dương -> 04/02/2000 âm (Canh Thìn)
        val lunar = LunarConverter.solarToLunar(2000, 3, 9)
        assertThat(lunar.day).isEqualTo(4)
        assertThat(lunar.month).isEqualTo(2)
        assertThat(lunar.year).isEqualTo(2000)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Bính Dần")
        assertThat(lunar.canChiMonth).isEqualTo("Kỷ Mão")
        assertThat(lunar.canChiYear).isEqualTo("Canh Thìn")
    }

    @Test
    fun testSolarToLunar_case9_newCentury2000() {
        // 01/01/2000 dương -> 25/11/1999 âm (Kỷ Mão)
        val lunar = LunarConverter.solarToLunar(2000, 1, 1)
        assertThat(lunar.day).isEqualTo(25)
        assertThat(lunar.month).isEqualTo(11)
        assertThat(lunar.year).isEqualTo(1999)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Mậu Ngọ")
        assertThat(lunar.canChiMonth).isEqualTo("Bính Tý")
        assertThat(lunar.canChiYear).isEqualTo("Kỷ Mão")
    }

    @Test
    fun testSolarToLunar_case10_historicDay1975() {
        // 30/04/1975 dương -> 20/03/1975 âm (Ất Mão)
        val lunar = LunarConverter.solarToLunar(1975, 4, 30)
        assertThat(lunar.day).isEqualTo(20)
        assertThat(lunar.month).isEqualTo(3)
        assertThat(lunar.year).isEqualTo(1975)
        assertThat(lunar.isLeapMonth).isFalse()
        assertThat(lunar.canChiDay).isEqualTo("Bính Ngọ")
        assertThat(lunar.canChiMonth).isEqualTo("Canh Thìn")
        assertThat(lunar.canChiYear).isEqualTo("Ất Mão")
    }

    @Test
    fun testLunarToSolar_allCases() {
        // Test chuyển ngược lại từ âm sang dương cho cả 10 ngày trên
        val cases = listOf(
            Triple(LunarDate(2022, 12, 10, false, "", "", ""), 2023, SolarDate(2023, 1, 1)),
            Triple(LunarDate(2023, 1, 1, false, "", "", ""), 2023, SolarDate(2023, 1, 22)),
            Triple(LunarDate(2023, 7, 15, false, "", "", ""), 2023, SolarDate(2023, 8, 30)),
            Triple(LunarDate(2023, 8, 15, false, "", "", ""), 2023, SolarDate(2023, 9, 29)),
            Triple(LunarDate(2025, 12, 18, false, "", "", ""), 2025, SolarDate(2026, 2, 5)),
            Triple(LunarDate(2026, 1, 1, false, "", "", ""), 2026, SolarDate(2026, 2, 17)),
            Triple(LunarDate(2020, 2, 29, false, "", "", ""), 2020, SolarDate(2020, 3, 22)),
            Triple(LunarDate(2000, 2, 4, false, "", "", ""), 2000, SolarDate(2000, 3, 9)),
            Triple(LunarDate(1999, 11, 25, false, "", "", ""), 1999, SolarDate(2000, 1, 1)),
            Triple(LunarDate(1975, 3, 20, false, "", "", ""), 1975, SolarDate(1975, 4, 30))
        )

        for (case in cases) {
            val lunarInput = case.first
            val expectedSolar = case.third
            val solar = LunarConverter.lunarToSolar(lunarInput.year, lunarInput.month, lunarInput.day, lunarInput.isLeapMonth)
            assertThat(solar.year).isEqualTo(expectedSolar.year)
            assertThat(solar.month).isEqualTo(expectedSolar.month)
            assertThat(solar.day).isEqualTo(expectedSolar.day)
        }
    }
}
