package com.example.lichvannien.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Định dạng YYYY-MM-DD, ví dụ: 2026-08-24
    val startTime: String? = null, // Ví dụ: "14:00"
    val endTime: String? = null, // Ví dụ: "15:30"
    val deadline: String? = null, // Ví dụ: "17/8" hoặc "17:00"
    val location: String? = null, // Ví dụ: "Văn phòng", "Nhà hàng Sen"
    val isCompleted: Boolean = false,
    val category: String = "DEFAULT", // WORK, PERSONAL, EVENT
    val colorHex: Long = 0xFFFFF2D9, // Màu sắc của thẻ pastel
    val colorTag: String = "PASTEL_YELLOW", // PASTEL_YELLOW, PASTEL_BLUE, PASTEL_MINT, PASTEL_PEACH, PASTEL_PINK, PASTEL_PURPLE
    val dueTime: String? = null, // Giờ hẹn (HH:mm), ví dụ: "06:30", "10:30"
    val repeatType: String = "DAILY", // ONCE, DAILY, WEEKDAYS, WEEKLY, MONTHLY, YEARLY
    val reminderType: String = "NOTIFICATION" // NOTIFICATION, ALARM
)

