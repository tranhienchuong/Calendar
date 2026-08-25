package com.example.lichvannien.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.AuspiciousResult
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TodayUiState(
    val today: LocalDate = LocalDate.now(),
    val lunarDate: LunarDate = LunarConverter.solarToLunar(LocalDate.now().year, LocalDate.now().monthValue, LocalDate.now().dayOfMonth),
    val auspicious: AuspiciousResult = AuspiciousCalculator.calculate(LunarConverter.solarToLunar(LocalDate.now().year, LocalDate.now().monthValue, LocalDate.now().dayOfMonth)),
    val specialDays: List<SpecialDay> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val isScheduleExpanded: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val specialDayRepository: SpecialDayRepository,
    private val taskRepository: TaskRepository,
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val reminderScheduler: com.example.lichvannien.ui.task.reminder.TaskReminderScheduler? = null
) : ViewModel() {

    private val _isScheduleExpanded = MutableStateFlow(true)
    private val today = LocalDate.now()
    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val lunarDate = lunarConverter.solarToLunar(today.year, today.monthValue, today.dayOfMonth)
    private val auspicious = auspiciousCalculator.calculate(lunarDate)

    private val todaySpecialDaysFlow = flow {
        val solarEvents = specialDayRepository.getEventsForSolarDate(today.monthValue, today.dayOfMonth)
        val lunarEvents = specialDayRepository.getEventsForLunarDate(lunarDate.month, lunarDate.day, lunarDate.isLeapMonth)
        emit(solarEvents + lunarEvents)
    }

    val uiState: StateFlow<TodayUiState> = combine(
        taskRepository.getTasksForDate(todayStr),
        todaySpecialDaysFlow,
        _isScheduleExpanded
    ) { tasks, specialDays, isExpanded ->
        TodayUiState(
            today = today,
            lunarDate = lunarDate,
            auspicious = auspicious,
            specialDays = specialDays,
            todayTasks = tasks,
            isScheduleExpanded = isExpanded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState(
            today = today,
            lunarDate = lunarDate,
            auspicious = auspicious
        )
    )

    init {
        refreshRecurringTasks()
    }

    fun refreshRecurringTasks(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val recurringTasks = taskRepository.getRecurringTasks()
            val tasksToUpdate = mutableListOf<Task>()
            for (task in recurringTasks) {
                val taskDate = com.example.lichvannien.ui.task.util.TaskDateTimeHelper.parseDate(task.date)
                if (taskDate != null && taskDate.isBefore(today)) {
                    val nextDate = com.example.lichvannien.ui.task.util.TaskDateTimeHelper.calculateNextActiveDate(task.date, task.repeatType, today)
                    val refreshedTask = task.copy(
                        date = nextDate,
                        isCompleted = false
                    )
                    tasksToUpdate.add(refreshedTask)
                    reminderScheduler?.scheduleTaskReminder(refreshedTask)
                }
            }
            if (tasksToUpdate.isNotEmpty()) {
                taskRepository.updateTasks(tasksToUpdate)
            }
        }
    }

    fun toggleScheduleExpanded() {
        _isScheduleExpanded.update { !it }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(id, isCompleted)
            val task = taskRepository.getTaskById(id)
            if (task != null) {
                if (isCompleted && task.repeatType.equals("ONCE", ignoreCase = true)) {
                    reminderScheduler?.cancelTaskReminder(id)
                } else {
                    reminderScheduler?.scheduleTaskReminder(task.copy(isCompleted = isCompleted))
                }
            }
        }
    }

    fun addNewTask(title: String, startTime: String?, endTime: String?, location: String?) {
        val finalDueTime = startTime?.ifBlank { null }
        viewModelScope.launch {
            val task = Task(
                title = title,
                date = todayStr,
                startTime = finalDueTime,
                dueTime = finalDueTime,
                endTime = endTime?.ifBlank { null },
                location = location?.ifBlank { null },
                isCompleted = false,
                category = "WORK",
                colorHex = 0xFF1976D2
            )
            val generatedId = taskRepository.addTask(task)
            if (generatedId > 0) {
                reminderScheduler?.scheduleTaskReminder(task.copy(id = generatedId))
            }
        }
    }
}
