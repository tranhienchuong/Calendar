package com.example.lichvannien.domain.model

data class AuspiciousResult(
    val isHoangDao: Boolean,
    val truc: String,            // "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thâu", "Khai", "Bế"
    val gioHoangDao: List<String>// ["Tý (23h-1h)", "Sửu (1h-3h)", ...]
)
