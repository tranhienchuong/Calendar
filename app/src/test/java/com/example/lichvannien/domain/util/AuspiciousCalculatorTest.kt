package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.LunarDate
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuspiciousCalculatorTest {

    @Test
    fun testCalculate_case1_tetQuyMao2023_hoangDao() {
        // Mùng 1 Tết Quý Mão (22/01/2023 dương -> 01/01/2023 âm)
        // Canh Thìn (Thìn = 5), tháng Giáp Dần (tháng 1)
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 1,
            isLeapMonth = false,
            canChiDay = "Canh Thìn",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        // Tháng 1 hoàng đạo có chi Thìn (5) -> true
        assertThat(result.isHoangDao).isTrue()
        // chiThang = (1+1)%12 = 2. chiNgay = 5. offset = (5-2+12)%12 = 3. TRUC[3] = "Bình"
        assertThat(result.truc).isEqualTo("Bình")
    }

    @Test
    fun testCalculate_case2_mung3Tet2023_hacDao() {
        // Mùng 3 Tết Quý Mão (24/01/2023 dương -> 03/01/2023 âm)
        // Nhâm Ngọ (Ngọ = 7), tháng Giáp Dần (tháng 1)
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 3,
            isLeapMonth = false,
            canChiDay = "Nhâm Ngọ",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        // Tháng 1 hoàng đạo KHÔNG có chi Ngọ (7) -> false
        assertThat(result.isHoangDao).isFalse()
    }

    @Test
    fun testCalculate_case3_trucKien() {
        // Ngày có chi trùng chiThang.
        // Tháng Giêng (month = 1) -> chiThang = 2. Chi ngày Sửu = 2 (index 1 + 1 = 2) -> Trực Kiến.
        // Ví dụ: Ngày 10 tháng Giêng âm (01/02/2023 dương) -> Kỷ Sửu
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 10,
            isLeapMonth = false,
            canChiDay = "Kỷ Sửu",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.truc).isEqualTo("Kiến")
    }

    @Test
    fun testCalculate_case4_trucTru() {
        // Tháng Giêng (month = 1) -> chiThang = 2. Chi ngày Dần = 3 (index 2 + 1 = 3) -> Trực Trừ.
        // Ví dụ: Ngày 11 tháng Giêng âm (02/02/2023 dương) -> Canh Dần
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 11,
            isLeapMonth = false,
            canChiDay = "Canh Dần",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.truc).isEqualTo("Trừ")
    }

    @Test
    fun testCalculate_case5_trucMan() {
        // Tháng Giêng (month = 1) -> chiThang = 2. Chi ngày Mão = 4 -> Trực Mãn.
        // Ví dụ: Ngày 12 tháng Giêng âm -> Tân Mão
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 12,
            isLeapMonth = false,
            canChiDay = "Tân Mão",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.truc).isEqualTo("Mãn")
    }

    @Test
    fun testCalculate_case6_hoursDayTy() {
        // Kiểm tra danh sách giờ hoàng đạo của ngày Tý (Sửu, Thìn, Tỵ, Mùi, Tuất)
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 13,
            isLeapMonth = false,
            canChiDay = "Nhâm Tý",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.gioHoangDao).hasSize(5)
        assertThat(result.gioHoangDao).containsExactly(
            "Sửu (1h-3h)",
            "Thìn (7h-9h)",
            "Tỵ (9h-11h)",
            "Mùi (13h-15h)",
            "Tuất (19h-21h)"
        ).inOrder()
    }

    @Test
    fun testCalculate_case7_hoursDaySuu() {
        // Kiểm tra danh sách giờ hoàng đạo của ngày Sửu (Tý, Dần, Mão, Ngọ, Thân)
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 10,
            isLeapMonth = false,
            canChiDay = "Kỷ Sửu",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.gioHoangDao).hasSize(5)
        assertThat(result.gioHoangDao).containsExactly(
            "Tý (23h-1h)",
            "Dần (3h-5h)",
            "Mão (5h-7h)",
            "Ngọ (11h-13h)",
            "Thân (15h-17h)"
        ).inOrder()
    }

    @Test
    fun testCalculate_case8_lastDayOfYear() {
        // Ngày cuối năm 29/12/2022 âm lịch (Mậu Ngọ)
        val lunar = LunarDate(
            year = 2022,
            month = 12,
            day = 29,
            isLeapMonth = false,
            canChiDay = "Mậu Ngọ",
            canChiMonth = "Quý Sửu",
            canChiYear = "Nhâm Dần"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        // Tháng 12 hoàng đạo có chi Ngọ (7) -> true
        assertThat(result.isHoangDao).isTrue()
        // chiThang = (12+1)%12 = 1. chiNgay = 7. offset = (7-1+12)%12 = 6. TRUC[6] = "Phá"
        assertThat(result.truc).isEqualTo("Phá")
    }

    @Test
    fun testCalculate_case9_firstDayOfYear() {
        // Mùng 1 tháng Giêng Quý Mão
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 1,
            isLeapMonth = false,
            canChiDay = "Canh Thìn",
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        val result = AuspiciousCalculator.calculate(lunar)

        assertThat(result.isHoangDao).isTrue()
        assertThat(result.truc).isEqualTo("Bình")
    }

    @Test
    fun testCalculate_case10_invalidCanChiInput() {
        // Input Can Chi bị lỗi
        val lunar = LunarDate(
            year = 2023,
            month = 1,
            day = 1,
            isLeapMonth = false,
            canChiDay = "", // Rỗng
            canChiMonth = "Giáp Dần",
            canChiYear = "Quý Mão"
        )
        
        // Đoạn code không được ném lỗi, phải trả về kết quả an toàn mặc định
        val result = AuspiciousCalculator.calculate(lunar)
        assertThat(result.truc).isNotNull()
        assertThat(result.gioHoangDao).isEmpty()
    }
}
