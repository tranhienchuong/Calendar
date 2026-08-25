package com.example.lichvannien.ui.task.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lichvannien.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var reminderScheduler: TaskReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allTasks = taskRepository.getAllTasks().firstOrNull() ?: emptyList()
                    val activeTasks = allTasks.filter { !it.isCompleted || !it.repeatType.equals("ONCE", ignoreCase = true) }
                    for (task in activeTasks) {
                        reminderScheduler.scheduleTaskReminder(task)
                    }
                } catch (_: Exception) {
                    // Safe catch on boot receiver
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
