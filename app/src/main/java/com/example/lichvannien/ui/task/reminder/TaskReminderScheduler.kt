package com.example.lichvannien.ui.task.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_REMINDER_ID = "channel_task_reminder"
        const val CHANNEL_REMINDER_NAME = "Lời nhắc công việc"
        const val CHANNEL_ALARM_ID = "channel_task_alarm"
        const val CHANNEL_ALARM_NAME = "Báo thức công việc"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Kênh thông báo thông thường
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                CHANNEL_REMINDER_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh gửi thông báo nhắc nhở việc cần làm"
                enableVibration(true)
            }

            // Kênh thông báo báo thức
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                CHANNEL_ALARM_NAME,
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Kênh chuông báo thức toàn màn hình cho công việc"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun scheduleTaskReminder(task: TaskEntity) {
        if (task.isCompleted) {
            cancelTaskReminder(task.id)
            return
        }

        val taskDate = TaskDateTimeHelper.parseDate(task.date) ?: LocalDate.now()
        val taskTime = TaskDateTimeHelper.parseTime(task.dueTime ?: task.startTime) ?: return

        var triggerDateTime = LocalDateTime.of(taskDate, taskTime)
        val now = LocalDateTime.now()

        // Nếu thời gian đã qua đối với nhắc nhở lặp lại hàng ngày, dời sang ngày tiếp theo
        if (triggerDateTime.isBefore(now)) {
            if (task.repeatType.equals("DAILY", ignoreCase = true)) {
                triggerDateTime = triggerDateTime.plusDays(1)
            } else {
                // Không lên lịch nếu là ONCE và đã quá hạn
                return
            }
        }

        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_REMINDER_TYPE, task.reminderType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Trường hợp thiếu quyền EXACT_ALARM trên Android 12+, dùng set thông thường
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }
}
