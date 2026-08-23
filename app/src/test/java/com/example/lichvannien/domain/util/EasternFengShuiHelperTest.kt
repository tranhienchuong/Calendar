package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.NguHanh
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EasternFengShuiHelperTest {

    @Test
    fun getZodiacInfo_computesCanChiAndNguHanhAccurately() {
        // 1995: Ất Hợi - Sơn Đầu Hỏa
        val atHoi = EasternFengShuiHelper.getZodiacInfo(1995)
        assertThat(atHoi.canChiYear).isEqualTo("Ất Hợi")
        assertThat(atHoi.animalEmoji).isEqualTo("🐷")
        assertThat(atHoi.nguHanh).isEqualTo(NguHanh.HOA)
        assertThat(atHoi.napAm).contains("Sơn Đầu Hỏa")

        // 1996: Bính Tý - Giản Hạ Thủy
        val binhTy = EasternFengShuiHelper.getZodiacInfo(1996)
        assertThat(binhTy.canChiYear).isEqualTo("Bính Tý")
        assertThat(binhTy.animalEmoji).isEqualTo("🐭")
        assertThat(binhTy.nguHanh).isEqualTo(NguHanh.THUY)
        assertThat(binhTy.napAm).contains("Giản Hạ Thủy")

        // 1990: Canh Ngọ - Lộ Bàng Thổ
        val canhNgo = EasternFengShuiHelper.getZodiacInfo(1990)
        assertThat(canhNgo.canChiYear).isEqualTo("Canh Ngọ")
        assertThat(canhNgo.animalEmoji).isEqualTo("🐴")
        assertThat(canhNgo.nguHanh).isEqualTo(NguHanh.THO)
        assertThat(canhNgo.napAm).contains("Lộ Bàng Thổ")

        // 1992: Nhâm Thân - Kiếm Phong Kim
        val nhamThan = EasternFengShuiHelper.getZodiacInfo(1992)
        assertThat(nhamThan.canChiYear).isEqualTo("Nhâm Thân")
        assertThat(nhamThan.animalEmoji).isEqualTo("🐵")
        assertThat(nhamThan.nguHanh).isEqualTo(NguHanh.KIM)
        assertThat(nhamThan.napAm).contains("Kiếm Phong Kim")

        // 1988: Mậu Thìn - Đại Lâm Mộc
        val mauThin = EasternFengShuiHelper.getZodiacInfo(1988)
        assertThat(mauThin.canChiYear).isEqualTo("Mậu Thìn")
        assertThat(mauThin.animalEmoji).isEqualTo("🐲")
        assertThat(mauThin.nguHanh).isEqualTo(NguHanh.MOC)
        assertThat(mauThin.napAm).contains("Đại Lâm Mộc")

        // 2026: Bính Ngọ - Thiên Hà Thủy
        val binhNgo2026 = EasternFengShuiHelper.getZodiacInfo(2026)
        assertThat(binhNgo2026.canChiYear).isEqualTo("Bính Ngọ")
        assertThat(binhNgo2026.animalEmoji).isEqualTo("🐴")
        assertThat(binhNgo2026.nguHanh).isEqualTo(NguHanh.THUY)
        assertThat(binhNgo2026.napAm).contains("Thiên Hà Thủy")
    }
}
