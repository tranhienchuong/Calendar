package com.example.lichvannien.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = AppHeaderBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm công việc"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: "Task - Việc cần làm"
            Text(
                text = stringResource(R.string.title_task_screen),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppHeaderBlue)
                }
            } else if (state.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có việc cần làm. Nhấn nút + để thêm mới!",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onToggle = { isDone -> viewModel.toggleTask(task.id, isDone) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }

                    item(key = "bottom_space") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        CreateTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, start, end, deadline, loc ->
                viewModel.addTask(
                    title = title,
                    startTime = start,
                    endTime = end,
                    deadline = deadline,
                    location = loc
                )
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskListItem(
    task: TaskEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOngoing = remember(task.startTime, task.endTime) {
        checkIfTaskIsOngoing(task.startTime, task.endTime)
    }

    val isDark = isSystemInDarkTheme()
    val ongoingBg = if (isDark) TaskOngoingBgDark else TaskOngoingBgLight
    val ongoingBorder = if (isDark) TaskOngoingGreen.copy(alpha = 0.8f) else TaskOngoingGreen

    val containerModifier = if (isOngoing && !task.isCompleted) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ongoingBg)
            .border(1.5.dp, ongoingBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle(!task.isCompleted) }
            .padding(horizontal = 4.dp, vertical = 6.dp)
    }

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = AppHeaderBlue,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 17.sp,
                    fontWeight = if (isOngoing && !task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    lineHeight = 22.sp
                )

                if (isOngoing && !task.isCompleted) {
                    Surface(
                        color = TaskOngoingGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ongoing_badge),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            val subtitleText = when {
                task.isCompleted -> stringResource(R.string.done_badge)
                task.deadline != null -> "Deadline: ${task.deadline}"
                task.startTime != null && task.endTime != null -> "${task.startTime} - ${task.endTime}"
                task.startTime != null -> "Thời gian: ${task.startTime}"
                !task.location.isNullOrBlank() -> task.location
                else -> null
            }

            if (subtitleText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    fontSize = 13.sp,
                    color = if (isOngoing && !task.isCompleted) TaskOngoingGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOngoing && !task.isCompleted) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Xóa",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun checkIfTaskIsOngoing(startStr: String?, endStr: String?): Boolean {
    if (startStr == null) return false
    return try {
        val now = LocalTime.now()
        val start = LocalTime.parse(startStr.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        val end = if (endStr != null) {
            LocalTime.parse(endStr.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            start.plusHours(1)
        }
        !now.isBefore(start) && now.isBefore(end)
    } catch (_: Exception) {
        false
    }
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, start: String?, end: String?, deadline: String?, location: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_task_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.hint_task_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Bắt đầu (09:00)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Kết thúc (11:30)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text(stringResource(R.string.hint_task_deadline)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.hint_task_location)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, startTime, endTime, deadline, location)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
