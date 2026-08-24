package com.example.lichvannien.ui.task.model

import androidx.compose.ui.graphics.Color

enum class TaskPastelColor(
    val tag: String,
    val displayName: String,
    val lightBg: Color,
    val darkBg: Color,
    val lightBorder: Color,
    val darkBorder: Color,
    val accentColor: Color,
    val colorHex: Long
) {
    YELLOW(
        tag = "PASTEL_YELLOW",
        displayName = "Vàng nhạt",
        lightBg = Color(0xFFFFF2D9),
        darkBg = Color(0xFF2C2417),
        lightBorder = Color(0xFFFDE68A),
        darkBorder = Color(0xFF534125),
        accentColor = Color(0xFFF59E0B),
        colorHex = 0xFFFFF2D9
    ),
    BLUE(
        tag = "PASTEL_BLUE",
        displayName = "Xanh dương",
        lightBg = Color(0xFFE0F2FE),
        darkBg = Color(0xFF182836),
        lightBorder = Color(0xFFBAE6FD),
        darkBorder = Color(0xFF1E3A5F),
        accentColor = Color(0xFF0284C7),
        colorHex = 0xFFE0F2FE
    ),
    MINT(
        tag = "PASTEL_MINT",
        displayName = "Xanh mint",
        lightBg = Color(0xFFDEF7EC),
        darkBg = Color(0xFF172D22),
        lightBorder = Color(0xFFA7F3D0),
        darkBorder = Color(0xFF1E4833),
        accentColor = Color(0xFF059669),
        colorHex = 0xFFDEF7EC
    ),
    PEACH(
        tag = "PASTEL_PEACH",
        displayName = "Cam đào",
        lightBg = Color(0xFFFFE4CE),
        darkBg = Color(0xFF33231A),
        lightBorder = Color(0xFFFED7AA),
        darkBorder = Color(0xFF5A3926),
        accentColor = Color(0xFFEA580C),
        colorHex = 0xFFFFE4CE
    ),
    PINK(
        tag = "PASTEL_PINK",
        displayName = "Hồng phấn",
        lightBg = Color(0xFFFCE7F3),
        darkBg = Color(0xFF321B29),
        lightBorder = Color(0xFFFBCFE8),
        darkBorder = Color(0xFF562843),
        accentColor = Color(0xFFDB2777),
        colorHex = 0xFFFCE7F3
    ),
    PURPLE(
        tag = "PASTEL_PURPLE",
        displayName = "Tím nhạt",
        lightBg = Color(0xFFF3E8FF),
        darkBg = Color(0xFF271A37),
        lightBorder = Color(0xFFDDD6FE),
        darkBorder = Color(0xFF452B66),
        accentColor = Color(0xFF7C3AED),
        colorHex = 0xFFF3E8FF
    );

    companion object {
        fun fromTag(tag: String?): TaskPastelColor {
            return entries.find { it.tag.equals(tag, ignoreCase = true) }
                ?: entries.find { it.name.equals(tag, ignoreCase = true) }
                ?: YELLOW
        }

        fun fromHex(hex: Long): TaskPastelColor {
            return entries.find { it.colorHex == hex } ?: YELLOW
        }
    }
}
