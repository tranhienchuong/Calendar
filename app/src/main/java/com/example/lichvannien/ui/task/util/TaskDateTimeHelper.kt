package com.example.lichvannien.ui.task.util

import com.example.lichvannien.data.local.entity.TaskEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class TaskRepeatRule(val code: String, val label: String, val shortLabel: String) {
    ONCE("ONCE", "Chỉ một lần", "Một lần"),
    DAILY("DAILY", "Hàng ngày", "Hàng ngày"),
    WEEKDAYS("WEEKDAYS", "Ngày trong tuần (T2 - T6)", "T2-T6"),
    WEEKLY("WEEKLY", "Hàng tuần", "Hàng tuần"),
    MONTHLY("MONTHLY", "Hàng tháng", "Hàng tháng"),
    YEARLY("YEARLY", "Hàng năm", "Hàng năm");

    companion object {
        fun fromCode(code: String?): TaskRepeatRule {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: DAILY
        }
    }
}

enum class TaskReminderMode(val code: String, val label: String) {
    NOTIFICATION("NOTIFICATION", "Lời nhắc thông báo"),
    ALARM("ALARM", "Lời nhắc báo thức");

    companion object {
        fun fromCode(code: String?): TaskReminderMode {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: NOTIFICATION
        }
    }
}

object TaskDateTimeHelper {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateStr.trim(), dateFormatter)
        } catch (_: Exception) {
            null
        }
    }

    fun parseTime(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            LocalTime.parse(timeStr.trim(), timeFormatter)
        } catch (_: Exception) {
            try {
                // Hỗ trợ định dạng H:mm hoặc H:m
                val parts = timeStr.trim().split(":")
                if (parts.size == 2) {
                    LocalTime.of(parts[0].toInt(), parts[1].toInt())
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    fun isOverdue(task: TaskEntity, now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (task.isCompleted) return false
        val taskDate = parseDate(task.date) ?: return false
        val taskTime = parseTime(task.dueTime ?: task.startTime)

        return if (taskTime != null) {
            val taskDateTime = LocalDateTime.of(taskDate, taskTime)
            taskDateTime.isBefore(now)
        } else {
            taskDate.isBefore(now.toLocalDate())
        }
    }

    fun formatTaskSubtitle(task: TaskEntity, now: LocalDateTime = LocalDateTime.now()): String {
        val taskDate = parseDate(task.date)
        val taskTime = parseTime(task.dueTime ?: task.startTime)
        val repeatRule = TaskRepeatRule.fromCode(task.repeatType)
        val isTaskOverdue = isOverdue(task, now)

        val dateLabel = when {
            taskDate == null -> ""
            taskDate == now.toLocalDate() -> "Hôm nay"
            taskDate == now.toLocalDate().plusDays(1) -> "Ngày mai"
            taskDate == now.toLocalDate().minusDays(1) -> "Hôm qua"
            else -> {
                val dayOfWeek = taskDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi"))
                "$dayOfWeek ${taskDate.dayOfMonth}/${taskDate.monthValue}"
            }
        }

        val timeStr = if (taskTime != null) {
            val hour = taskTime.hour
            val minute = if (taskTime.minute < 10) "0${taskTime.minute}" else "${taskTime.minute}"
            "$hour:$minute"
        } else if (!task.startTime.isNullOrBlank()) {
            task.startTime
        } else null

        val repeatLabel = if (repeatRule != TaskRepeatRule.ONCE) repeatRule.shortLabel else null

        return when {
            isTaskOverdue -> {
                if (timeStr != null) {
                    "$dateLabel $timeStr | Quá hạn"
                } else {
                    "$dateLabel | Quá hạn"
                }
            }
            timeStr != null && repeatLabel != null -> {
                "$dateLabel $timeStr,$repeatLabel"
            }
            timeStr != null -> {
                "$dateLabel $timeStr"
            }
            repeatLabel != null -> {
                "$dateLabel, $repeatLabel"
            }
            dateLabel.isNotBlank() -> {
                dateLabel
            }
            else -> {
                task.location ?: ""
            }
        }
    }

    /**
     * Tính toán ngày tiếp theo cho công việc lặp lại dựa trên ngày hiện tại của task hoặc ngày hôm nay.
     */
    fun calculateNextOccurrenceDate(
        currentDateStr: String?,
        repeatType: String,
        baseDate: LocalDate = LocalDate.now()
    ): String {
        val rule = TaskRepeatRule.fromCode(repeatType)
        val currentLocalDate = parseDate(currentDateStr) ?: baseDate

        // Đảm bảo điểm bắt đầu tính toán ít nhất là từ ngày hôm nay (nếu task bị quá hạn từ các ngày trước)
        val anchorDate = if (currentLocalDate.isBefore(baseDate)) baseDate else currentLocalDate

        val nextDate = when (rule) {
            TaskRepeatRule.ONCE -> anchorDate
            TaskRepeatRule.DAILY -> anchorDate.plusDays(1)
            TaskRepeatRule.WEEKDAYS -> {
                var next = anchorDate.plusDays(1)
                while (next.dayOfWeek.value > 5) { // 6 = Saturday, 7 = Sunday
                    next = next.plusDays(1)
                }
                next
            }
            TaskRepeatRule.WEEKLY -> anchorDate.plusWeeks(1)
            TaskRepeatRule.MONTHLY -> anchorDate.plusMonths(1)
            TaskRepeatRule.YEARLY -> anchorDate.plusYears(1)
        }

        return nextDate.format(dateFormatter)
    }
}
