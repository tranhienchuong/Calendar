package com.example.lichvannien.ui.task.util

import com.example.lichvannien.domain.model.Task
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

    fun isOverdue(task: Task, now: LocalDateTime = LocalDateTime.now()): Boolean {
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

    fun formatTaskSubtitle(task: Task, now: LocalDateTime = LocalDateTime.now()): String {
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
     * Tính toán ngày kích hoạt tiếp theo cho một task lặp lại trong quá khứ khi bước sang ngày mới (today).
     * Đảm bảo task lặp lại từ hôm qua hoặc các ngày trước sẽ tự động chuyển sang hôm nay (hoặc chu kỳ tiếp theo).
     */
    fun calculateNextActiveDate(
        currentDateStr: String?,
        repeatType: String,
        today: LocalDate = LocalDate.now()
    ): String {
        val rule = TaskRepeatRule.fromCode(repeatType)
        val taskDate = parseDate(currentDateStr) ?: today

        if (!taskDate.isBefore(today)) {
            return taskDate.format(dateFormatter)
        }

        val nextDate = when (rule) {
            TaskRepeatRule.ONCE -> taskDate
            TaskRepeatRule.DAILY -> today
            TaskRepeatRule.WEEKDAYS -> {
                var d = today
                while (d.dayOfWeek.value > 5) { // 6 = Saturday, 7 = Sunday
                    d = d.plusDays(1)
                }
                d
            }
            TaskRepeatRule.WEEKLY -> {
                val targetDayOfWeek = taskDate.dayOfWeek
                var d = today
                while (d.dayOfWeek != targetDayOfWeek) {
                    d = d.plusDays(1)
                }
                d
            }
            TaskRepeatRule.MONTHLY -> {
                var d = taskDate
                while (d.isBefore(today)) {
                    d = d.plusMonths(1)
                }
                d
            }
            TaskRepeatRule.YEARLY -> {
                var d = taskDate
                while (d.isBefore(today)) {
                    d = d.plusYears(1)
                }
                d
            }
        }

        return nextDate.format(dateFormatter)
    }

    /**
     * Tính toán thời điểm kích hoạt báo thức tiếp theo (ngày + giờ) trong tương lai.
     * Trả về null nếu task là ONCE và đã quá hạn.
     */
    fun calculateNextTriggerDateTime(
        taskDate: LocalDate,
        taskTime: LocalTime,
        repeatType: String,
        now: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime? {
        val rule = TaskRepeatRule.fromCode(repeatType)
        var triggerDateTime = LocalDateTime.of(taskDate, taskTime)

        if (rule == TaskRepeatRule.ONCE) {
            return if (triggerDateTime.isBefore(now)) null else triggerDateTime
        }

        if (rule == TaskRepeatRule.WEEKDAYS) {
            while (triggerDateTime.dayOfWeek.value > 5) {
                triggerDateTime = triggerDateTime.plusDays(1)
            }
        }

        if (!triggerDateTime.isBefore(now)) {
            return triggerDateTime
        }

        return when (rule) {
            TaskRepeatRule.ONCE -> null
            TaskRepeatRule.DAILY -> {
                var dt = LocalDateTime.of(now.toLocalDate(), taskTime)
                if (!dt.isAfter(now)) {
                    dt = dt.plusDays(1)
                }
                dt
            }
            TaskRepeatRule.WEEKDAYS -> {
                var dt = LocalDateTime.of(now.toLocalDate(), taskTime)
                if (!dt.isAfter(now)) {
                    dt = dt.plusDays(1)
                }
                while (dt.dayOfWeek.value > 5) {
                    dt = dt.plusDays(1)
                }
                dt
            }
            TaskRepeatRule.WEEKLY -> {
                val targetDayOfWeek = taskDate.dayOfWeek
                var dt = LocalDateTime.of(now.toLocalDate(), taskTime)
                while (dt.dayOfWeek != targetDayOfWeek || !dt.isAfter(now)) {
                    dt = dt.plusDays(1)
                }
                dt
            }
            TaskRepeatRule.MONTHLY -> {
                var dt = triggerDateTime
                while (!dt.isAfter(now)) {
                    dt = dt.plusMonths(1)
                }
                dt
            }
            TaskRepeatRule.YEARLY -> {
                var dt = triggerDateTime
                while (!dt.isAfter(now)) {
                    dt = dt.plusYears(1)
                }
                dt
            }
        }
    }
}
