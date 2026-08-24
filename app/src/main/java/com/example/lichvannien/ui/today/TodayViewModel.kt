package com.example.lichvannien.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.model.AuspiciousResult
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val todayTasks: List<TaskEntity> = emptyList(),
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

    val uiState: StateFlow<TodayUiState> = combine(
        taskRepository.getTasksForDate(todayStr),
        _isScheduleExpanded
    ) { tasks, isExpanded ->
        val lunar = lunarConverter.solarToLunar(today.year, today.monthValue, today.dayOfMonth)
        val auspicious = auspiciousCalculator.calculate(lunar)
        val solarEvents = specialDayRepository.getEventsForSolarDate(today.monthValue, today.dayOfMonth)
        val lunarEvents = specialDayRepository.getEventsForLunarDate(lunar.month, lunar.day, lunar.isLeapMonth)
        val allSpecialDays = solarEvents + lunarEvents

        TodayUiState(
            today = today,
            lunarDate = lunar,
            auspicious = auspicious,
            specialDays = allSpecialDays,
            todayTasks = tasks,
            isScheduleExpanded = isExpanded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState()
    )

    fun toggleScheduleExpanded() {
        _isScheduleExpanded.update { !it }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(id)
            if (task == null) {
                taskRepository.toggleTaskCompleted(id, isCompleted)
                return@launch
            }

            val repeatRule = com.example.lichvannien.ui.task.util.TaskRepeatRule.fromCode(task.repeatType)
            if (isCompleted && repeatRule != com.example.lichvannien.ui.task.util.TaskRepeatRule.ONCE) {
                val nextDate = com.example.lichvannien.ui.task.util.TaskDateTimeHelper.calculateNextOccurrenceDate(task.date, task.repeatType)
                val updatedTask = task.copy(
                    date = nextDate,
                    isCompleted = false
                )
                taskRepository.updateTask(updatedTask)
                reminderScheduler?.scheduleTaskReminder(updatedTask)
            } else {
                taskRepository.toggleTaskCompleted(id, isCompleted)
                if (isCompleted) {
                    reminderScheduler?.cancelTaskReminder(id)
                } else {
                    reminderScheduler?.scheduleTaskReminder(task.copy(isCompleted = false))
                }
            }
        }
    }

    fun addNewTask(title: String, startTime: String?, endTime: String?, location: String?) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                date = todayStr,
                startTime = startTime?.ifBlank { null },
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
