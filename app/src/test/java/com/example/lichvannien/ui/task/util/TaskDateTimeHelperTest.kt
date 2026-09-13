package com.example.lichvannien.ui.task.util

import com.example.lichvannien.domain.model.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskDateTimeHelperTest {

    private val fixedNow = LocalDateTime.of(2026, 8, 24, 10, 0) // 10:00 AM on 2026-08-24

    @Test
    fun isOverdue_taskCompleted_returnsFalse() {
        val task = Task(
            title = "Đi chợ",
            date = "2026-08-23",
            dueTime = "08:00",
            isCompleted = true
        )
        val overdue = TaskDateTimeHelper.isOverdue(task, fixedNow)
        assertThat(overdue).isFalse()
    }

    @Test
    fun isOverdue_pastDate_returnsTrue() {
        val task = Task(
            title = "Nộp báo cáo",
            date = "2026-08-23",
            dueTime = "14:00",
            isCompleted = false
        )
        val overdue = TaskDateTimeHelper.isOverdue(task, fixedNow)
        assertThat(overdue).isTrue()
    }

    @Test
    fun isOverdue_sameDateEarlierTime_returnsTrue() {
        val task = Task(
            title = "Họp sáng",
            date = "2026-08-24",
            dueTime = "08:30",
            isCompleted = false
        )
        val overdue = TaskDateTimeHelper.isOverdue(task, fixedNow)
        assertThat(overdue).isTrue()
    }

    @Test
    fun isOverdue_sameDateLaterTime_returnsFalse() {
        val task = Task(
            title = "Học Trade",
            date = "2026-08-24",
            dueTime = "13:00",
            isCompleted = false
        )
        val overdue = TaskDateTimeHelper.isOverdue(task, fixedNow)
        assertThat(overdue).isFalse()
    }

    @Test
    fun formatTaskSubtitle_todayWithTimeAndDailyRepeat() {
        val task = Task(
            title = "Cắm cơm",
            date = "2026-08-24",
            dueTime = "10:30",
            repeatType = "DAILY",
            isCompleted = false
        )
        val subtitle = TaskDateTimeHelper.formatTaskSubtitle(task, fixedNow)
        assertThat(subtitle).isEqualTo("Hôm nay 10:30,Hàng ngày")
    }

    @Test
    fun formatTaskSubtitle_tomorrowWithTimeAndDailyRepeat() {
        val task = Task(
            title = "Dậy",
            date = "2026-08-25",
            dueTime = "06:30",
            repeatType = "DAILY",
            isCompleted = false
        )
        val subtitle = TaskDateTimeHelper.formatTaskSubtitle(task, fixedNow)
        assertThat(subtitle).isEqualTo("Ngày mai 6:30,Hàng ngày")
    }

    @Test
    fun calculateNextActiveDate_pastDaily_returnsToday() {
        val next = TaskDateTimeHelper.calculateNextActiveDate(
            currentDateStr = "2026-08-23", // yesterday
            repeatType = "DAILY",
            today = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-08-24") // today
    }

    @Test
    fun calculateNextActiveDate_pastWeekdaysFromFriday_returnsMonday() {
        val next = TaskDateTimeHelper.calculateNextActiveDate(
            currentDateStr = "2026-08-21", // Friday
            repeatType = "WEEKDAYS",
            today = LocalDate.of(2026, 8, 24) // Monday
        )
        assertThat(next).isEqualTo("2026-08-24")
    }

    @Test
    fun calculateNextActiveDate_futureOrTodayTask_keepsOriginalDate() {
        val next = TaskDateTimeHelper.calculateNextActiveDate(
            currentDateStr = "2026-08-25",
            repeatType = "DAILY",
            today = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-08-25")
    }

    @Test
    fun calculateNextTriggerDateTime_onceFuture_returnsSameDateTime() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "ONCE",
            now = fixedNow // 10:00 AM on 2026-08-24
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 24, 14, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_oncePast_returnsNull() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(8, 0),
            repeatType = "ONCE",
            now = fixedNow // 10:00 AM on 2026-08-24
        )
        assertThat(next).isNull()
    }

    @Test
    fun calculateNextTriggerDateTime_dailyPast_returnsTomorrow() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(8, 0),
            repeatType = "DAILY",
            now = fixedNow // 10:00 AM on 2026-08-24 (Monday)
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 25, 8, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_weekdaysFromFridayPast_returnsMonday() {
        val fridayNow = LocalDateTime.of(2026, 8, 28, 17, 0) // Friday 17:00
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 28),
            taskTime = java.time.LocalTime.of(9, 0),
            repeatType = "WEEKDAYS",
            now = fridayNow
        )
        // Saturday 29, Sunday 30 -> Next is Monday Aug 31
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 31, 9, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_weeklyPast_returnsNextWeek() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(8, 0),
            repeatType = "WEEKLY",
            now = fixedNow // Monday 10:00 AM
        )
        // Next Monday is Aug 31
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 31, 8, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_onceCompleted_returnsNull() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "ONCE",
            now = fixedNow,
            isCompleted = true
        )
        assertThat(next).isNull()
    }

    @Test
    fun calculateNextTriggerDateTime_dailyCompletedTodayFutureTime_returnsTomorrow() {
        // Task is scheduled for 14:00 today (future compared to fixedNow 10:00 AM)
        // But user already marked it completed -> should advance to tomorrow 14:00
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "DAILY",
            now = fixedNow,
            isCompleted = true
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 25, 14, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_weekdaysCompletedFridayFutureTime_returnsMonday() {
        val fridayNow = LocalDateTime.of(2026, 8, 28, 10, 0)
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 28),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "WEEKDAYS",
            now = fridayNow,
            isCompleted = true
        )
        // Friday is done -> Next weekday is Monday Aug 31
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 31, 14, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_weeklyCompletedMondayFutureTime_returnsNextMonday() {
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 24),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "WEEKLY",
            now = fixedNow, // Monday 10:00 AM
            isCompleted = true
        )
        // Monday is done -> Next Monday is Aug 31
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 31, 14, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_dailyPastCompletedYesterday_returnsTodayIfNotPassed() {
        // Completed yesterday, today at 10:00 AM, task is scheduled for 14:00 today -> should return today 14:00
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 23),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "DAILY",
            now = fixedNow, // 2026-08-24 10:00 AM
            isCompleted = true
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 24, 14, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_dailyPastCompletedYesterday_returnsTomorrowIfPassed() {
        // Completed yesterday, today at 10:00 AM, task was scheduled for 08:00 (past) -> should return tomorrow 08:00
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 23),
            taskTime = java.time.LocalTime.of(8, 0),
            repeatType = "DAILY",
            now = fixedNow, // 2026-08-24 10:00 AM
            isCompleted = true
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 25, 8, 0))
    }

    @Test
    fun calculateNextTriggerDateTime_weekdaysPastCompletedFriday_returnsMonday() {
        // Completed on Friday Aug 21, now is Monday Aug 24 10:00 AM, task is for 14:00 -> should return Monday 14:00
        val next = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = LocalDate.of(2026, 8, 21),
            taskTime = java.time.LocalTime.of(14, 0),
            repeatType = "WEEKDAYS",
            now = fixedNow, // Monday 2026-08-24 10:00 AM
            isCompleted = true
        )
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 8, 24, 14, 0))
    }
}
