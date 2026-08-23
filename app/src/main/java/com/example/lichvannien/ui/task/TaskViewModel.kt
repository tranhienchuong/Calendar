package com.example.lichvannien.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<TaskUiState> = taskRepository.getAllTasks()
        .map { tasks ->
            TaskUiState(tasks = tasks, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState(isLoading = true)
        )

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(id, isCompleted)
        }
    }

    fun addTask(
        title: String,
        date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        startTime: String? = null,
        endTime: String? = null,
        deadline: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                date = date,
                startTime = startTime?.ifBlank { null },
                endTime = endTime?.ifBlank { null },
                deadline = deadline?.ifBlank { null },
                location = location?.ifBlank { null },
                isCompleted = false,
                category = "WORK",
                colorHex = 0xFF1976D2
            )
            taskRepository.addTask(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }
}
