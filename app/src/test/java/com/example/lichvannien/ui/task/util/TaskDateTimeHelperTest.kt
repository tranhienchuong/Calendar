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
}
