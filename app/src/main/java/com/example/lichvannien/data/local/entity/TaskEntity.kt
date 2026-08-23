package com.example.lichvannien.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Định dạng YYYY-MM-DD, ví dụ: 2026-08-15
    val startTime: String? = null, // Ví dụ: "14:00"
    val endTime: String? = null, // Ví dụ: "15:30"
    val deadline: String? = null, // Ví dụ: "17/8" hoặc "17:00"
    val location: String? = null, // Ví dụ: "Văn phòng", "Nhà hàng Sen"
    val isCompleted: Boolean = false,
    val category: String = "DEFAULT", // WORK, PERSONAL, EVENT
    val colorHex: Long = 0xFF1976D2 // Màu sắc của thanh đánh dấu bên trái
)
