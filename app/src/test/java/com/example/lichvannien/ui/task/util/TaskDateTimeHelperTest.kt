package com.example.lichvannien.ui.task.util

import com.example.lichvannien.data.local.entity.TaskEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskDateTimeHelperTest {

    private val fixedNow = LocalDateTime.of(2026, 8, 24, 10, 0) // 10:00 AM on 2026-08-24

    @Test
    fun isOverdue_taskCompleted_returnsFalse() {
        val task = TaskEntity(
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
        val task = TaskEntity(
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
        val task = TaskEntity(
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
        val task = TaskEntity(
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
        val task = TaskEntity(
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
        val task = TaskEntity(
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
    fun calculateNextOccurrenceDate_daily_returnsNextDay() {
        val next = TaskDateTimeHelper.calculateNextOccurrenceDate(
            currentDateStr = "2026-08-24",
            repeatType = "DAILY",
            baseDate = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-08-25")
    }

    @Test
    fun calculateNextOccurrenceDate_weekdaysOnFriday_returnsNextMonday() {
        // 2026-08-28 is Friday
        val next = TaskDateTimeHelper.calculateNextOccurrenceDate(
            currentDateStr = "2026-08-28",
            repeatType = "WEEKDAYS",
            baseDate = LocalDate.of(2026, 8, 28)
        )
        assertThat(next).isEqualTo("2026-08-31") // Monday
    }

    @Test
    fun calculateNextOccurrenceDate_weekly_returnsNextWeek() {
        val next = TaskDateTimeHelper.calculateNextOccurrenceDate(
            currentDateStr = "2026-08-24",
            repeatType = "WEEKLY",
            baseDate = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-08-31")
    }

    @Test
    fun calculateNextOccurrenceDate_monthly_returnsNextMonth() {
        val next = TaskDateTimeHelper.calculateNextOccurrenceDate(
            currentDateStr = "2026-08-24",
            repeatType = "MONTHLY",
            baseDate = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-09-24")
    }

    @Test
    fun calculateNextOccurrenceDate_overduePastDate_anchorsFromToday() {
        // Task was from 2026-08-10, today is 2026-08-24. Next daily task should be 2026-08-25
        val next = TaskDateTimeHelper.calculateNextOccurrenceDate(
            currentDateStr = "2026-08-10",
            repeatType = "DAILY",
            baseDate = LocalDate.of(2026, 8, 24)
        )
        assertThat(next).isEqualTo("2026-08-25")
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
}
