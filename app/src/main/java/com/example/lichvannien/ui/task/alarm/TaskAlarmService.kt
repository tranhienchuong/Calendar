package com.example.lichvannien.ui.task.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.lichvannien.MainActivity
import com.example.lichvannien.R
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.ui.task.reminder.TaskReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskAlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "com.example.lichvannien.action.START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.lichvannien.action.STOP_ALARM"
        const val ACTION_SNOOZE = "com.example.lichvannien.action.SNOOZE"
        const val ACTION_DISMISS = "com.example.lichvannien.action.DISMISS"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"

        private const val NOTIFICATION_ID = 9999
        private const val AUTO_SILENCE_MS = 10 * 60 * 1000L // 10 phút tự động ngắt

        fun startAlarm(context: Context, taskId: Long, taskTitle: String) {
            val intent = Intent(context, TaskAlarmService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, TaskAlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var reminderScheduler: TaskReminderScheduler

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentTaskId: Long = -1L
    private var currentTaskTitle: String = "Báo thức công việc"

    private val autoSilenceRunnable = Runnable {
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START_ALARM -> {
                currentTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                currentTaskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Báo thức công việc"

                startForeground(NOTIFICATION_ID, buildAlarmNotification(currentTaskId, currentTaskTitle))
                playAlarmSoundAndVibrate()

                handler.removeCallbacks(autoSilenceRunnable)
                handler.postDelayed(autoSilenceRunnable, AUTO_SILENCE_MS)
            }
            ACTION_STOP_ALARM -> {
                stopAlarmAndSelf()
            }
            ACTION_SNOOZE -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, currentTaskId)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: currentTaskTitle
                if (taskId > 0) {
                    reminderScheduler.scheduleSnooze(taskId, taskTitle, 5)
                }
                stopAlarmAndSelf()
            }
            ACTION_DISMISS -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, currentTaskId)
                if (taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        taskRepository.toggleTaskCompleted(taskId, true)
                    }
                }
                stopAlarmAndSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun playAlarmSoundAndVibrate() {
        stopSoundAndVibration()

        // 1. Phát chuông lặp lại liên tục bằng MediaPlayer
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@TaskAlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            // Fallback nếu MediaPlayer gặp sự cố
        }

        // 2. Rung theo nhịp báo động liên tục
        try {
            val pattern = longArrayOf(0, 800, 400, 800, 400, 1200)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 = repeat indefinitely
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {}
    }

    private fun stopSoundAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }

    private fun stopAlarmAndSelf() {
        handler.removeCallbacks(autoSilenceRunnable)
        stopSoundAndVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildAlarmNotification(taskId: Long, taskTitle: String): Notification {
        // Intent mở TaskAlarmActivity toàn màn hình
        val fullScreenIntent = Intent(this, TaskAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            taskId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Báo lại 5 phút
        val snoozeIntent = Intent(this, TaskAlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            1001,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Tắt & Hoàn thành
        val dismissIntent = Intent(this, TaskAlarmService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1002,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TaskReminderScheduler.CHANNEL_ALARM_SERVICE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ BÁO THỨC CÔNG VIỆC")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, "💤 Báo lại 5p", snoozePendingIntent)
            .addAction(0, "⏹️ Tắt", dismissPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopSoundAndVibration()
        handler.removeCallbacks(autoSilenceRunnable)
        super.onDestroy()
    }
}
