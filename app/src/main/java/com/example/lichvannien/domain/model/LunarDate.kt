package com.example.lichvannien.domain.model

data class LunarDate(
    val year: Int,           // năm âm lịch
    val month: Int,          // tháng âm (1-12)
    val day: Int,            // ngày âm (1-30)
    val isLeapMonth: Boolean,// tháng nhuận hay không
    val canChiDay: String,   // ví dụ: "Canh Thân"
    val canChiMonth: String, // ví dụ: "Giáp Tý"
    val canChiYear: String   // ví dụ: "Quý Mão"
)
