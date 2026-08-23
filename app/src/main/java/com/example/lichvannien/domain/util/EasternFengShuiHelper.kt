package com.example.lichvannien.domain.util

import com.example.lichvannien.domain.model.EasternZodiacInfo
import com.example.lichvannien.domain.model.NguHanh

object EasternFengShuiHelper {

    private val THIEN_CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val DIA_CHI = listOf(
        Pair("Tý", "🐭 Chuột"),
        Pair("Sửu", "🐮 Trâu"),
        Pair("Dần", "🐯 Hổ"),
        Pair("Mão", "🐱 Mèo"),
        Pair("Thìn", "🐲 Rồng"),
        Pair("Tỵ", "🐍 Rắn"),
        Pair("Ngọ", "🐴 Ngựa"),
        Pair("Mùi", "🐐 Dê"),
        Pair("Thân", "🐵 Khỉ"),
        Pair("Dậu", "🐔 Gà"),
        Pair("Tuất", "🐶 Chó"),
        Pair("Hợi", "🐷 Lợn")
    )

    private val NAP_AM_MAP = mapOf(
        "Giáp Tý" to Pair(NguHanh.KIM, "Hải Trung Kim (Vàng dưới biển)"),
        "Ất Sửu" to Pair(NguHanh.KIM, "Hải Trung Kim (Vàng dưới biển)"),
        "Bính Dần" to Pair(NguHanh.HOA, "Lư Trung Hỏa (Lửa trong lò)"),
        "Đinh Mão" to Pair(NguHanh.HOA, "Lư Trung Hỏa (Lửa trong lò)"),
        "Mậu Thìn" to Pair(NguHanh.MOC, "Đại Lâm Mộc (Gỗ rừng già)"),
        "Kỷ Tỵ" to Pair(NguHanh.MOC, "Đại Lâm Mộc (Gỗ rừng già)"),
        "Canh Ngọ" to Pair(NguHanh.THO, "Lộ Bàng Thổ (Đất ven đường)"),
        "Tân Mùi" to Pair(NguHanh.THO, "Lộ Bàng Thổ (Đất ven đường)"),
        "Nhâm Thân" to Pair(NguHanh.KIM, "Kiếm Phong Kim (Vàng mũi kiếm)"),
        "Quý Dậu" to Pair(NguHanh.KIM, "Kiếm Phong Kim (Vàng mũi kiếm)"),
        "Giáp Tuất" to Pair(NguHanh.HOA, "Sơn Đầu Hỏa (Lửa trên núi)"),
        "Ất Hợi" to Pair(NguHanh.HOA, "Sơn Đầu Hỏa (Lửa trên núi)"),
        "Bính Tý" to Pair(NguHanh.THUY, "Giản Hạ Thủy (Nước dưới khe)"),
        "Đinh Sửu" to Pair(NguHanh.THUY, "Giản Hạ Thủy (Nước dưới khe)"),
        "Mậu Dần" to Pair(NguHanh.THO, "Thành Đầu Thổ (Đất trên thành)"),
        "Kỷ Mão" to Pair(NguHanh.THO, "Thành Đầu Thổ (Đất trên thành)"),
        "Canh Thìn" to Pair(NguHanh.KIM, "Bạch Lạp Kim (Vàng chân đèn)"),
        "Tân Tỵ" to Pair(NguHanh.KIM, "Bạch Lạp Kim (Vàng chân đèn)"),
        "Nhâm Ngọ" to Pair(NguHanh.MOC, "Dương Liễu Mộc (Gỗ cây dương liễu)"),
        "Quý Mùi" to Pair(NguHanh.MOC, "Dương Liễu Mộc (Gỗ cây dương liễu)"),
        "Giáp Thân" to Pair(NguHanh.THUY, "Tuyền Trung Thủy (Nước trong suối)"),
        "Ất Dậu" to Pair(NguHanh.THUY, "Tuyền Trung Thủy (Nước trong suối)"),
        "Bính Tuất" to Pair(NguHanh.THO, "Ốc Thượng Thổ (Đất nóc nhà)"),
        "Đinh Hợi" to Pair(NguHanh.THO, "Ốc Thượng Thổ (Đất nóc nhà)"),
        "Mậu Tý" to Pair(NguHanh.HOA, "Tích Lịch Hỏa (Lửa sấm sét)"),
        "Kỷ Sửu" to Pair(NguHanh.HOA, "Tích Lịch Hỏa (Lửa sấm sét)"),
        "Canh Dần" to Pair(NguHanh.MOC, "Tùng Bách Mộc (Gỗ cây tùng bách)"),
        "Tân Mão" to Pair(NguHanh.MOC, "Tùng Bách Mộc (Gỗ cây tùng bách)"),
        "Nhâm Thìn" to Pair(NguHanh.THUY, "Trường Lưu Thủy (Nước sông dài)"),
        "Quý Tỵ" to Pair(NguHanh.THUY, "Trường Lưu Thủy (Nước sông dài)"),
        "Giáp Ngọ" to Pair(NguHanh.KIM, "Sa Trung Kim (Vàng trong cát)"),
        "Ất Mùi" to Pair(NguHanh.KIM, "Sa Trung Kim (Vàng trong cát)"),
        "Bính Thân" to Pair(NguHanh.HOA, "Sơn Hạ Hỏa (Lửa dưới núi)"),
        "Đinh Dậu" to Pair(NguHanh.HOA, "Sơn Hạ Hỏa (Lửa dưới núi)"),
        "Mậu Tuất" to Pair(NguHanh.MOC, "Bình Địa Mộc (Gỗ đồng bằng)"),
        "Kỷ Hợi" to Pair(NguHanh.MOC, "Bình Địa Mộc (Gỗ đồng bằng)"),
        "Canh Tý" to Pair(NguHanh.THO, "Bích Thượng Thổ (Đất trên tường)"),
        "Tân Sửu" to Pair(NguHanh.THO, "Bích Thượng Thổ (Đất trên tường)"),
        "Nhâm Dần" to Pair(NguHanh.KIM, "Kim Bạch Kim (Vàng dát mỏng)"),
        "Quý Mão" to Pair(NguHanh.KIM, "Kim Bạch Kim (Vàng dát mỏng)"),
        "Giáp Thìn" to Pair(NguHanh.HOA, "Phúc Đăng Hỏa (Lửa đèn dầu)"),
        "Ất Tỵ" to Pair(NguHanh.HOA, "Phúc Đăng Hỏa (Lửa đèn dầu)"),
        "Bính Ngọ" to Pair(NguHanh.THUY, "Thiên Hà Thủy (Nước trên trời)"),
        "Đinh Mùi" to Pair(NguHanh.THUY, "Thiên Hà Thủy (Nước trên trời)"),
        "Mậu Thân" to Pair(NguHanh.THO, "Đại Trạch Thổ (Đất nền nhà)"),
        "Kỷ Dậu" to Pair(NguHanh.THO, "Đại Trạch Thổ (Đất nền nhà)"),
        "Canh Tuất" to Pair(NguHanh.KIM, "Thoa Xuyến Kim (Vàng trang sức)"),
        "Tân Hợi" to Pair(NguHanh.KIM, "Thoa Xuyến Kim (Vàng trang sức)"),
        "Nhâm Tý" to Pair(NguHanh.MOC, "Tang Đố Mộc (Gỗ cây dâu)"),
        "Quý Sửu" to Pair(NguHanh.MOC, "Tang Đố Mộc (Gỗ cây dâu)"),
        "Giáp Dần" to Pair(NguHanh.THUY, "Đại Khê Thủy (Nước khe lớn)"),
        "Ất Mão" to Pair(NguHanh.THUY, "Đại Khê Thủy (Nước khe lớn)"),
        "Bính Thìn" to Pair(NguHanh.THO, "Sa Trung Thổ (Đất pha cát)"),
        "Đinh Tỵ" to Pair(NguHanh.THO, "Sa Trung Thổ (Đất pha cát)"),
        "Mậu Ngọ" to Pair(NguHanh.HOA, "Thiên Thượng Hỏa (Lửa trên trời)"),
        "Kỷ Mùi" to Pair(NguHanh.HOA, "Thiên Thượng Hỏa (Lửa trên trời)"),
        "Canh Thân" to Pair(NguHanh.MOC, "Thạch Lựu Mộc (Gỗ cây lựu đá)"),
        "Tân Dậu" to Pair(NguHanh.MOC, "Thạch Lựu Mộc (Gỗ cây lựu đá)"),
        "Nhâm Tuất" to Pair(NguHanh.THUY, "Đại Hải Thủy (Nước biển lớn)"),
        "Quý Hợi" to Pair(NguHanh.THUY, "Đại Hải Thủy (Nước biển lớn)")
    )

    fun getCanChiYearName(year: Int): String {
        val canIndex = (year + 6) % 10
        val chiIndex = (year + 8) % 12
        val can = THIEN_CAN[(canIndex + 10) % 10]
        val chi = DIA_CHI[(chiIndex + 12) % 12].first
        return "$can $chi"
    }

    fun getZodiacInfo(birthYear: Int): EasternZodiacInfo {
        val year = if (birthYear in 1900..2100) birthYear else 2000
        val canIndex = ((year + 6) % 10 + 10) % 10
        val chiIndex = ((year + 8) % 12 + 12) % 12

        val can = THIEN_CAN[canIndex]
        val chiPair = DIA_CHI[chiIndex]
        val canChi = "$can ${chiPair.first}"

        val napAmData = NAP_AM_MAP[canChi] ?: Pair(NguHanh.KIM, "Hải Trung Kim")
        val nguHanh = napAmData.first
        val napAm = napAmData.second

        val luckyColors = when (nguHanh) {
            NguHanh.KIM -> listOf("Trắng", "Xám bạc", "Vàng ánh kim", "Nâu đất")
            NguHanh.MOC -> listOf("Xanh lá cây", "Xanh lục", "Đen", "Xanh nước biển")
            NguHanh.THUY -> listOf("Đen", "Xanh dương", "Trắng", "Ánh kim")
            NguHanh.HOA -> listOf("Đỏ", "Hồng", "Tím", "Cam", "Xanh lá cây")
            NguHanh.THO -> listOf("Vàng sậm", "Nâu đất", "Đỏ", "Hồng", "Tím")
        }

        val suitableElements = when (nguHanh) {
            NguHanh.KIM -> listOf("Thổ sinh Kim (Rất tốt)", "Kim hợp Kim")
            NguHanh.MOC -> listOf("Thủy sinh Mộc (Rất tốt)", "Mộc hợp Mộc")
            NguHanh.THUY -> listOf("Kim sinh Thủy (Rất tốt)", "Thủy hợp Thủy")
            NguHanh.HOA -> listOf("Mộc sinh Hỏa (Rất tốt)", "Hỏa hợp Hỏa")
            NguHanh.THO -> listOf("Hỏa sinh Thổ (Rất tốt)", "Thổ hợp Thổ")
        }

        val parts = chiPair.second.split(" ")
        val animalEmoji = parts[0]
        val animalName = "Tuổi ${chiPair.first} ($animalEmoji ${parts.getOrNull(1) ?: ""})".trim()

        val description = "Người sinh năm $canChi ($year) mang Mệnh ${nguHanh.nameVi} - $napAm. Hợp với các màu sắc thuộc hành tương sinh và tương hợp."

        return EasternZodiacInfo(
            year = year,
            canChiYear = canChi,
            animalName = animalName,
            animalEmoji = animalEmoji,
            nguHanh = nguHanh,
            napAm = napAm,
            luckyColors = luckyColors,
            suitableElements = suitableElements,
            description = description
        )
    }
}
