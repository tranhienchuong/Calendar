package com.example.lichvannien.ui.task.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.lichvannien.domain.model.Task
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
        const val CHANNEL_REMINDER_ID = "channel_task_reminder_v2"
        const val CHANNEL_REMINDER_NAME = "Lời nhắc công việc"
        const val CHANNEL_ALARM_ID = "channel_task_alarm_v2"
        const val CHANNEL_ALARM_NAME = "Báo thức công việc"
        const val CHANNEL_ALARM_SERVICE_ID = "channel_task_alarm_service_v2"
        const val CHANNEL_ALARM_SERVICE_NAME = "Dịch vụ chuông báo thức"

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

            // Xóa các kênh cũ nếu có để áp dụng cấu hình âm thanh mới
            try {
                notificationManager.deleteNotificationChannel("channel_task_reminder")
                notificationManager.deleteNotificationChannel("channel_task_alarm")
                notificationManager.deleteNotificationChannel("channel_task_alarm_service")
            } catch (_: Exception) {}

            val notifSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notifAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Kênh thông báo thông thường (NOTIFICATION)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                CHANNEL_REMINDER_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh gửi thông báo nhắc nhở việc cần làm"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(notifSoundUri, notifAudioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarmAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 2. Kênh thông báo báo thức (ALARM)
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                CHANNEL_ALARM_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh chuông báo thức cho công việc"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 1200)
                setSound(alarmSoundUri, alarmAudioAttributes)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // 3. Kênh im lặng cho Foreground Service báo thức (để MediaPlayer độc quyền phát chuông không bị trộn âm thanh)
            val alarmServiceChannel = NotificationChannel(
                CHANNEL_ALARM_SERVICE_ID,
                CHANNEL_ALARM_SERVICE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh hiển thị trạng thái đang reo chuông báo thức"
                enableVibration(false)
                setSound(null, null)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(alarmServiceChannel)
        }
    }

    fun scheduleTaskReminder(task: Task) {
        if (task.isCompleted && task.repeatType.equals("ONCE", ignoreCase = true)) {
            cancelTaskReminder(task.id)
            return
        }

        val taskDate = TaskDateTimeHelper.parseDate(task.date) ?: LocalDate.now()
        val taskTime = TaskDateTimeHelper.parseTime(task.dueTime ?: task.startTime) ?: return
        val now = LocalDateTime.now()

        val triggerDateTime = TaskDateTimeHelper.calculateNextTriggerDateTime(
            taskDate = taskDate,
            taskTime = taskTime,
            repeatType = task.repeatType,
            now = now
        ) ?: run {
            cancelTaskReminder(task.id)
            return
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

        val showAppIntent = Intent(context, com.example.lichvannien.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            showAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            // Sử dụng setAlarmClock: chuẩn Android cao nhất cho báo thức và lời nhắc,
            // giúp đánh thức CPU, unfreeze ứng dụng trên mọi thiết bị (Vivo, Xiaomi, Samsung...) ngay cả khi tắt màn hình
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
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
            } catch (_: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun scheduleSnooze(taskId: Long, taskTitle: String, minutes: Int = 5) {
        val triggerMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_REMINDER_TYPE, "ALARM")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showAppIntent = Intent(context, com.example.lichvannien.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            showAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
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
            } catch (_: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            }
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
