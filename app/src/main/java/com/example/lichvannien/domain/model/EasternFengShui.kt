package com.example.lichvannien.domain.model

enum class NguHanh(val nameVi: String, val emoji: String) {
    KIM("Kim", "⚔️"),
    MOC("Mộc", "🌿"),
    THUY("Thủy", "💧"),
    HOA("Hỏa", "🔥"),
    THO("Thổ", "⛰️")
}

data class EasternZodiacInfo(
    val year: Int,
    val canChiYear: String,
    val animalName: String,
    val animalEmoji: String,
    val nguHanh: NguHanh,
    val napAm: String,
    val luckyColors: List<String>,
    val suitableElements: List<String>,
    val description: String
)
