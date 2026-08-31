package com.example.lichvannien.ui.task.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lichvannien.MainActivity
import com.example.lichvannien.R
import com.example.lichvannien.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

import com.example.lichvannien.ui.task.alarm.TaskAlarmActivity
import com.example.lichvannien.ui.task.alarm.TaskAlarmService
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper

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

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task == null) {
                    // Công việc đã bị xóa, không phát chuông hay thông báo
                    return@launch
                }

                val now = LocalDateTime.now()
                val today = now.toLocalDate()
                val taskDate = TaskDateTimeHelper.parseDate(task.date) ?: today

                // Nếu công việc đã được đánh dấu hoàn thành trong chu kỳ hôm nay (hoặc trước đó):
                if (task.isCompleted && !taskDate.isAfter(today)) {
                    // Không phát chuông hay gửi thông báo
                    // Nếu là task lặp lại, đảm bảo chu kỳ ngày mai / tiếp theo được lên lịch sẵn sàng
                    if (!task.repeatType.equals("ONCE", ignoreCase = true)) {
                        reminderScheduler.scheduleTaskReminder(task)
                    }
                    return@launch
                }

                val effectiveTitle = task.title.ifBlank { taskTitle }
                val effectiveReminderType = task.reminderType.ifBlank { reminderType }
                val isAlarm = effectiveReminderType.equals("ALARM", ignoreCase = true)

                if (isAlarm) {
                    // Đảm bảo CPU hoạt động và đánh thức thiết bị khi có báo thức
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    val wakeLock = powerManager?.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "LichVanNien:TaskAlarmReceiver"
                    )
                    wakeLock?.acquire(15 * 1000L)

                    // 1. Khởi chạy Foreground Service phát chuông báo thức liên tục và rung
                    TaskAlarmService.startAlarm(context, taskId, effectiveTitle)

                    // 2. Mở màn hình TaskAlarmActivity toàn màn hình đè lên màn hình khóa
                    try {
                        val alarmIntent = Intent(context, TaskAlarmActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(TaskAlarmService.EXTRA_TASK_ID, taskId)
                            putExtra(TaskAlarmService.EXTRA_TASK_TITLE, effectiveTitle)
                        }
                        context.startActivity(alarmIntent)
                    } catch (_: Exception) {}

                    try {
                        if (wakeLock?.isHeld == true) wakeLock.release()
                    } catch (_: Exception) {}
                } else {
                    // Chế độ THÔNG BÁO thường
                    showStandardNotification(context, taskId, effectiveTitle)
                }

                // Lên lịch cho chu kỳ tiếp theo nếu task có chu kỳ lặp lại (giữ nguyên ngày của task hôm nay trong DB)
                if (!task.repeatType.equals("ONCE", ignoreCase = true)) {
                    reminderScheduler.scheduleTaskReminder(task)
                }
            } catch (_: Exception) {
                // Safe catch
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showStandardNotification(
        context: Context,
        taskId: Long,
        taskTitle: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, TaskReminderScheduler.CHANNEL_REMINDER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📌 Lời nhắc công việc")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(taskId.toInt(), notificationBuilder.build())
        } catch (_: SecurityException) {
            // Trường hợp thiếu quyền POST_NOTIFICATIONS trên Android 13+
        }
    }
}
