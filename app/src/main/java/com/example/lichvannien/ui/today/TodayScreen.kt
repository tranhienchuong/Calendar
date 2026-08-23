package com.example.lichvannien.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun TodayScreen(
    onDayDetailClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val lunarCardBg = if (isDark) LunarCardBgDark else LunarCardBgLight

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
                    contentDescription = "Thêm lịch trình"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Card: Thứ, Ngày lớn (Dương lịch) & Khối Âm lịch
            item(key = "top_today_card") {
                TodayHeaderCard(
                    state = state,
                    lunarCardBg = lunarCardBg,
                    onCardClick = {
                        onDayDetailClick(
                            state.today.year,
                            state.today.monthValue,
                            state.today.dayOfMonth
                        )
                    }
                )
            }

            // 2. Card Giờ Hoàng Đạo
            item(key = "auspicious_hours_card") {
                AuspiciousHoursCard(state = state)
            }

            // 3. Card Sự kiện & Ngày đặc biệt (Thay cho Thời tiết & Lập kế hoạch tuần)
            item(key = "special_events_card") {
                SpecialEventsTodayCard(state = state)
            }

            // 4. Lịch trình hôm nay (Today's Schedule)
            item(key = "today_schedule_card") {
                TodayScheduleSection(
                    tasks = state.todayTasks,
                    isExpanded = state.isScheduleExpanded,
                    onToggleExpand = { viewModel.toggleScheduleExpanded() },
                    onTaskToggle = { id, isDone -> viewModel.toggleTask(id, isDone) },
                    onAddTaskClick = { showAddTaskDialog = true }
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddTaskDialog) {
        AddTodayTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, start, end, loc ->
                viewModel.addNewTask(title, start, end, loc)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TodayHeaderCard(
    state: TodayUiState,
    lunarCardBg: Color,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeekName = when (state.today.dayOfWeek.value) {
        1 -> "Thứ Hai"
        2 -> "Thứ Ba"
        3 -> "Thứ Tư"
        4 -> "Thứ Năm"
        5 -> "Thứ Sáu"
        6 -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Thứ, Số ngày Dương to lớn màu xanh, Tháng & Năm
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayOfWeekName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.today.dayOfMonth}",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    color = AppHeaderBlue,
                    lineHeight = 70.sp
                )
                Text(
                    text = "Tháng ${state.today.monthValue}, ${state.today.year}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bên phải: Khối bo góc Âm lịch & Can Chi
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(lunarCardBg)
                    .padding(vertical = 24.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${state.lunarDate.day}/${state.lunarDate.month}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Âm lịch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "(${state.lunarDate.canChiDay})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AuspiciousHoursCard(
    state: TodayUiState,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Giờ Hoàng Đạo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            val hoursText = if (state.auspicious.gioHoangDao.isNotEmpty()) {
                "Giờ - " + state.auspicious.gioHoangDao.joinToString(", ")
            } else {
                "Giờ - 5:00, 9:00, 12:00, 15:00-21:00"
            }
            Text(
                text = hoursText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SpecialEventsTodayCard(
    state: TodayUiState,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = OrangeSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.card_special_events_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.specialDays.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_special_event_today),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.specialDays.forEach { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = event.icon ?: "⭐",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = event.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppHeaderBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayScheduleSection(
    tasks: List<TaskEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTaskToggle: (Long, Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_schedule_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tasks.isEmpty()) {
                        Text(
                            text = "Chưa có lịch trình cho hôm nay. Nhấn nút + để thêm!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        tasks.forEach { task ->
                            TodayScheduleItem(
                                task = task,
                                onTaskToggle = onTaskToggle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayScheduleItem(
    task: TaskEntity,
    onTaskToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOngoing = remember(task.startTime, task.endTime) {
        checkIfTaskIsOngoing(task.startTime, task.endTime)
    }

    val isDark = isSystemInDarkTheme()
    val ongoingBg = if (isDark) TaskOngoingBgDark else TaskOngoingBgLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOngoing) ongoingBg else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Color Indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isOngoing) TaskOngoingGreen else Color(task.colorHex))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val timePrefix = when {
                    task.startTime != null && task.endTime != null -> "${task.startTime} - "
                    task.startTime != null -> "${task.startTime} - "
                    task.deadline != null -> "Deadline: ${task.deadline} - "
                    else -> ""
                }
                Text(
                    text = "$timePrefix${task.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (isOngoing) {
                    Surface(
                        color = TaskOngoingGreen,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ongoing_badge),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!task.location.isNullOrBlank()) {
                Text(
                    text = task.location,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onTaskToggle(task.id, it) },
            colors = CheckboxDefaults.colors(
                checkedColor = AppHeaderBlue
            )
        )
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
fun AddTodayTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, startTime: String?, endTime: String?, location: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
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
                        label = { Text("Bắt đầu (14:00)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Kết thúc (15:30)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
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
                        onConfirm(title, startTime, endTime, location)
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
