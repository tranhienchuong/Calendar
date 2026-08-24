package com.example.lichvannien.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.ui.task.model.TaskPastelColor
import com.example.lichvannien.ui.task.reminder.TaskReminderScheduler
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper
import com.example.lichvannien.ui.task.util.TaskRepeatRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class TaskFilter(val label: String) {
    ALL("Tất cả"),
    PENDING("Chưa xong"),
    COMPLETED("Đã xong"),
    TODAY("Hôm nay"),
    OVERDUE("Quá hạn")
}

enum class TaskSort(val label: String) {
    TIME_ASC("Giờ tăng dần"),
    TIME_DESC("Giờ giảm dần"),
    TITLE("Tên việc A-Z"),
    COLOR("Theo màu sắc")
}

data class TaskUiState(
    val allTasks: List<TaskEntity> = emptyList(),
    val displayedTasks: List<TaskEntity> = emptyList(),
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0,
    val searchQuery: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: TaskSort = TaskSort.TIME_ASC,
    val isLoading: Boolean = false
) {
    // Tương thích ngược với các màn hình hoặc test cũ
    val tasks: List<TaskEntity> get() = displayedTasks
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: TaskReminderScheduler? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _sort = MutableStateFlow(TaskSort.TIME_ASC)

    val uiState: StateFlow<TaskUiState> = combine(
        taskRepository.getAllTasks(),
        _searchQuery,
        _filter,
        _sort
    ) { tasks, query, filter, sort ->
        val now = LocalDateTime.now()
        val todayStr = now.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val pending = tasks.count { !it.isCompleted }
        val completed = tasks.count { it.isCompleted }
        val overdue = tasks.count { TaskDateTimeHelper.isOverdue(it, now) }

        // 1. Lọc theo search query
        var list = if (query.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.location?.contains(query, ignoreCase = true) == true
            }
        }

        // 2. Lọc theo danh mục Filter
        list = when (filter) {
            TaskFilter.ALL -> list
            TaskFilter.PENDING -> list.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> list.filter { it.isCompleted }
            TaskFilter.TODAY -> list.filter { it.date == todayStr }
            TaskFilter.OVERDUE -> list.filter { TaskDateTimeHelper.isOverdue(it, now) }
        }

        // 3. Sắp xếp theo Sort
        list = when (sort) {
            TaskSort.TIME_ASC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.date }
                    .thenBy { it.dueTime ?: it.startTime ?: "99:99" }
            )
            TaskSort.TIME_DESC -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.date }
                    .thenByDescending { it.dueTime ?: it.startTime ?: "00:00" }
            )
            TaskSort.TITLE -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.title.lowercase() }
            )
            TaskSort.COLOR -> list.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.colorTag }
            )
        }

        TaskUiState(
            allTasks = tasks,
            displayedTasks = list,
            totalCount = tasks.size,
            pendingCount = pending,
            completedCount = completed,
            overdueCount = overdue,
            searchQuery = query,
            filter = filter,
            sort = sort,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState(isLoading = true)
    )

    init {
        refreshRecurringTasks()
    }

    fun refreshRecurringTasks(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val recurringTasks = taskRepository.getRecurringTasks()
            val tasksToUpdate = mutableListOf<TaskEntity>()
            for (task in recurringTasks) {
                val taskDate = TaskDateTimeHelper.parseDate(task.date)
                if (taskDate != null && taskDate.isBefore(today)) {
                    val nextDate = TaskDateTimeHelper.calculateNextActiveDate(task.date, task.repeatType, today)
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

    fun setSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.update { filter }
    }

    fun setSort(sort: TaskSort) {
        _sort.update { sort }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(id, isCompleted)
            if (isCompleted) {
                reminderScheduler?.cancelTaskReminder(id)
            } else {
                val task = taskRepository.getTaskById(id)
                if (task != null) {
                    reminderScheduler?.scheduleTaskReminder(task.copy(isCompleted = false))
                }
            }
        }
    }

    fun addTask(
        title: String,
        date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        startTime: String? = null,
        endTime: String? = null,
        deadline: String? = null,
        location: String? = null,
        colorTag: String = "PASTEL_YELLOW",
        dueTime: String? = null,
        repeatType: String = "DAILY",
        reminderType: String = "NOTIFICATION"
    ) {
        val pastel = TaskPastelColor.fromTag(colorTag)
        val finalDueTime = dueTime ?: startTime

        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                date = date,
                startTime = finalDueTime?.ifBlank { null },
                endTime = endTime?.ifBlank { null },
                deadline = deadline?.ifBlank { null },
                location = location?.ifBlank { null },
                isCompleted = false,
                category = "WORK",
                colorHex = pastel.colorHex,
                colorTag = pastel.tag,
                dueTime = finalDueTime?.ifBlank { null },
                repeatType = repeatType,
                reminderType = reminderType
            )
            val generatedId = taskRepository.addTask(task)
            if (generatedId > 0) {
                reminderScheduler?.scheduleTaskReminder(task.copy(id = generatedId))
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            if (task.isCompleted) {
                reminderScheduler?.cancelTaskReminder(task.id)
            } else {
                reminderScheduler?.scheduleTaskReminder(task)
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            reminderScheduler?.cancelTaskReminder(id)
            taskRepository.deleteTask(id)
        }
    }
}
