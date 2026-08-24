package com.example.lichvannien.data.mapper

import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.model.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskMapperTest {

    @Test
    fun entityToDomain_mapsAllFieldsCorrectly() {
        val entity = TaskEntity(
            id = 42L,
            title = "Họp dự án",
            date = "2026-08-24",
            startTime = "09:00",
            endTime = "10:30",
            deadline = "10:30",
            location = "Phòng họp 1",
            isCompleted = true,
            category = "WORK",
            colorHex = 0xFF1976D2,
            colorTag = "PASTEL_BLUE",
            dueTime = "09:00",
            repeatType = "WEEKDAYS",
            reminderType = "ALARM"
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(42L)
        assertThat(domain.title).isEqualTo("Họp dự án")
        assertThat(domain.date).isEqualTo("2026-08-24")
        assertThat(domain.startTime).isEqualTo("09:00")
        assertThat(domain.endTime).isEqualTo("10:30")
        assertThat(domain.deadline).isEqualTo("10:30")
        assertThat(domain.location).isEqualTo("Phòng họp 1")
        assertThat(domain.isCompleted).isTrue()
        assertThat(domain.category).isEqualTo("WORK")
        assertThat(domain.colorHex).isEqualTo(0xFF1976D2)
        assertThat(domain.colorTag).isEqualTo("PASTEL_BLUE")
        assertThat(domain.dueTime).isEqualTo("09:00")
        assertThat(domain.repeatType).isEqualTo("WEEKDAYS")
        assertThat(domain.reminderType).isEqualTo("ALARM")
    }

    @Test
    fun domainToEntity_mapsAllFieldsCorrectly() {
        val domain = Task(
            id = 99L,
            title = "Đi siêu thị",
            date = "2026-08-25",
            startTime = "18:00",
            endTime = "19:00",
            deadline = null,
            location = "WinMart",
            isCompleted = false,
            category = "PERSONAL",
            colorHex = 0xFF4CAF50,
            colorTag = "PASTEL_MINT",
            dueTime = "18:00",
            repeatType = "ONCE",
            reminderType = "NOTIFICATION"
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(99L)
        assertThat(entity.title).isEqualTo("Đi siêu thị")
        assertThat(entity.date).isEqualTo("2026-08-25")
        assertThat(entity.startTime).isEqualTo("18:00")
        assertThat(entity.endTime).isEqualTo("19:00")
        assertThat(entity.deadline).isNull()
        assertThat(entity.location).isEqualTo("WinMart")
        assertThat(entity.isCompleted).isFalse()
        assertThat(entity.category).isEqualTo("PERSONAL")
        assertThat(entity.colorHex).isEqualTo(0xFF4CAF50)
        assertThat(entity.colorTag).isEqualTo("PASTEL_MINT")
        assertThat(entity.dueTime).isEqualTo("18:00")
        assertThat(entity.repeatType).isEqualTo("ONCE")
        assertThat(entity.reminderType).isEqualTo("NOTIFICATION")
    }
}
