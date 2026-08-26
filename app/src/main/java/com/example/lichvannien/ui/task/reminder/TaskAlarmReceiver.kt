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
import javax.inject.Inject

import com.example.lichvannien.ui.task.alarm.TaskAlarmActivity
import com.example.lichvannien.ui.task.alarm.TaskAlarmService

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

        val isAlarm = reminderType.equals("ALARM", ignoreCase = true)

        if (isAlarm) {
            // Đảm bảo CPU hoạt động và đánh thức thiết bị khi có báo thức
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "LichVanNien:TaskAlarmReceiver"
            )
            wakeLock?.acquire(15 * 1000L)

            // 1. Khởi chạy Foreground Service phát chuông báo thức liên tục và rung
            TaskAlarmService.startAlarm(context, taskId, taskTitle)

            // 2. Mở màn hình TaskAlarmActivity toàn màn hình đè lên màn hình khóa
            try {
                val alarmIntent = Intent(context, TaskAlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(TaskAlarmService.EXTRA_TASK_ID, taskId)
                    putExtra(TaskAlarmService.EXTRA_TASK_TITLE, taskTitle)
                }
                context.startActivity(alarmIntent)
            } catch (_: Exception) {}

            try {
                if (wakeLock?.isHeld == true) wakeLock.release()
            } catch (_: Exception) {}
        } else {
            // Chế độ THÔNG BÁO thường: Giữ nguyên thiết kế ban đầu (chỉ gửi notification nhẹ nhàng lên thanh trạng thái)
            showStandardNotification(context, taskId, taskTitle)
        }

        // Lên lịch cho chu kỳ tiếp theo nếu task có chu kỳ lặp lại (giữ nguyên ngày của task hôm nay trong DB)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null && !task.repeatType.equals("ONCE", ignoreCase = true)) {
                    // scheduleTaskReminder tự động tính toán thời điểm reo tiếp theo (ví dụ: ngày mai) và nạp vào AlarmManager
                    // mà KHÔNG làm thay đổi ngày của task trong Database để task hôm nay hiển thị đúng trạng thái Quá hạn
                    reminderScheduler.scheduleTaskReminder(task)
                }
            } catch (_: Exception) {
                // Xử lý an toàn khi receiver chạy
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
