package com.example.lichvannien.ui.task.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lichvannien.MainActivity
import com.example.lichvannien.R
import com.example.lichvannien.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var reminderScheduler: TaskReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_TITLE) ?: "Nhắc nhở công việc"
        val reminderType = intent.getStringExtra(TaskReminderScheduler.EXTRA_REMINDER_TYPE) ?: "NOTIFICATION"

        if (taskId == -1L) return

        showTaskNotification(context, taskId, taskTitle, reminderType)

        // Tự động lên lịch lại nếu task có chu kỳ lặp lại
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null && !task.isCompleted) {
                    if (!task.repeatType.equals("ONCE", ignoreCase = true)) {
                        reminderScheduler.scheduleTaskReminder(task)
                    }
                }
            } catch (_: Exception) {
                // Xử lý an toàn khi receiver chạy
            }
        }
    }

    private fun showTaskNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        reminderType: String
    ) {
        val isAlarm = reminderType.equals("ALARM", ignoreCase = true)
        val channelId = if (isAlarm) {
            TaskReminderScheduler.CHANNEL_ALARM_ID
        } else {
            TaskReminderScheduler.CHANNEL_REMINDER_ID
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (isAlarm) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Lời nhắc việc cần làm")
            .setContentText(taskTitle)
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(taskId.toInt(), notificationBuilder.build())
        } catch (_: SecurityException) {
            // Trường hợp thiếu quyền POST_NOTIFICATIONS trên Android 13+
        }
    }
}
