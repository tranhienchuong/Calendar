package com.example.lichvannien.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZodiacHelperTest {

    @Test
    fun testAllZodiacSignsCorrectlyMapped() {
        // Aries (Bạch Dương): 21/03 - 19/04
        assertThat(ZodiacHelper.getZodiacSign(21, 3)?.nameEn).isEqualTo("Aries")
        assertThat(ZodiacHelper.getZodiacSign(19, 4)?.nameEn).isEqualTo("Aries")

        // Taurus (Kim Ngưu): 20/04 - 20/05
        assertThat(ZodiacHelper.getZodiacSign(20, 4)?.nameEn).isEqualTo("Taurus")
        assertThat(ZodiacHelper.getZodiacSign(20, 5)?.nameEn).isEqualTo("Taurus")

        // Gemini (Song Tử): 21/05 - 20/06
        assertThat(ZodiacHelper.getZodiacSign(21, 5)?.nameEn).isEqualTo("Gemini")
        assertThat(ZodiacHelper.getZodiacSign(20, 6)?.nameEn).isEqualTo("Gemini")

        // Cancer (Cự Giải): 21/06 - 22/07
        assertThat(ZodiacHelper.getZodiacSign(21, 6)?.nameEn).isEqualTo("Cancer")
        assertThat(ZodiacHelper.getZodiacSign(22, 7)?.nameEn).isEqualTo("Cancer")

        // Leo (Sư Tử): 23/07 - 22/08
        assertThat(ZodiacHelper.getZodiacSign(23, 7)?.nameEn).isEqualTo("Leo")
        assertThat(ZodiacHelper.getZodiacSign(22, 8)?.nameEn).isEqualTo("Leo")

        // Virgo (Xử Nữ): 23/08 - 22/09
        assertThat(ZodiacHelper.getZodiacSign(23, 8)?.nameEn).isEqualTo("Virgo")
        assertThat(ZodiacHelper.getZodiacSign(22, 9)?.nameEn).isEqualTo("Virgo")

        // Libra (Thiên Bình): 23/09 - 22/10
        assertThat(ZodiacHelper.getZodiacSign(23, 9)?.nameEn).isEqualTo("Libra")
        assertThat(ZodiacHelper.getZodiacSign(22, 10)?.nameEn).isEqualTo("Libra")

        // Scorpio (Bọ Cạp): 23/10 - 21/11
        assertThat(ZodiacHelper.getZodiacSign(23, 10)?.nameEn).isEqualTo("Scorpio")
        assertThat(ZodiacHelper.getZodiacSign(21, 11)?.nameEn).isEqualTo("Scorpio")

        // Sagittarius (Nhân Mã): 22/11 - 21/12
        assertThat(ZodiacHelper.getZodiacSign(22, 11)?.nameEn).isEqualTo("Sagittarius")
        assertThat(ZodiacHelper.getZodiacSign(21, 12)?.nameEn).isEqualTo("Sagittarius")

        // Capricorn (Ma Kết): 22/12 - 19/01
        assertThat(ZodiacHelper.getZodiacSign(22, 12)?.nameEn).isEqualTo("Capricorn")
        assertThat(ZodiacHelper.getZodiacSign(31, 12)?.nameEn).isEqualTo("Capricorn")
        assertThat(ZodiacHelper.getZodiacSign(1, 1)?.nameEn).isEqualTo("Capricorn")
        assertThat(ZodiacHelper.getZodiacSign(19, 1)?.nameEn).isEqualTo("Capricorn")

        // Aquarius (Bảo Bình): 20/01 - 18/02
        assertThat(ZodiacHelper.getZodiacSign(20, 1)?.nameEn).isEqualTo("Aquarius")
        assertThat(ZodiacHelper.getZodiacSign(18, 2)?.nameEn).isEqualTo("Aquarius")

        // Pisces (Song Ngư): 19/02 - 20/03
        assertThat(ZodiacHelper.getZodiacSign(19, 2)?.nameEn).isEqualTo("Pisces")
        assertThat(ZodiacHelper.getZodiacSign(20, 3)?.nameEn).isEqualTo("Pisces")
    }

    @Test
    fun testGetZodiacSignForInvalidDateReturnsNull() {
        assertThat(ZodiacHelper.getZodiacSign(0, 5)).isNull()
        assertThat(ZodiacHelper.getZodiacSign(32, 5)).isNull()
        assertThat(ZodiacHelper.getZodiacSign(15, 0)).isNull()
        assertThat(ZodiacHelper.getZodiacSign(15, 13)).isNull()
    }

    @Test
    fun testValidBirthdays() {
        assertThat(ZodiacHelper.isValidDate(1, 1)).isTrue()
        assertThat(ZodiacHelper.isValidDate(29, 2)).isTrue() // Chấp nhận 29/2 cho năm nhuận bất kỳ
        assertThat(ZodiacHelper.isValidDate(30, 2)).isFalse() // Tháng 2 không bao giờ có ngày 30
        assertThat(ZodiacHelper.isValidDate(31, 4)).isFalse() // Tháng 4 chỉ có 30 ngày
        assertThat(ZodiacHelper.isValidDate(31, 12)).isTrue()
        assertThat(ZodiacHelper.isValidDate(32, 12)).isFalse()
    }
}
