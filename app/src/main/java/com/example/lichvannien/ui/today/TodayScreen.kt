package com.example.lichvannien.ui.today

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.theme.*
import com.example.lichvannien.ui.task.components.TaskItemCard
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
    val fabColor = Color(0xFFFBBF24)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = fabColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = fabColor, spotColor = fabColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm lịch trình",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White
                )
            }
        },
        containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF9FAFB),
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
                    onCardClick = {
                        onDayDetailClick(
                            state.today.year,
                            state.today.monthValue,
                            state.today.dayOfMonth
                        )
                    }
                )
            }

            // 2. Card Giờ Hoàng Đạo (Pastel Mint Theme)
            item(key = "auspicious_hours_card") {
                AuspiciousHoursCard(state = state)
            }

            // 3. Card Sự kiện & Ngày đặc biệt (Pastel Yellow Theme)
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
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val dayOfWeekName = when (state.today.dayOfWeek.value) {
        1 -> "Thứ Hai"
        2 -> "Thứ Ba"
        3 -> "Thứ Tư"
        4 -> "Thứ Năm"
        5 -> "Thứ Sáu"
        6 -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF1E1E1E) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Thứ, Số ngày Dương to lớn màu vàng cam, Tháng & Năm
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayOfWeekName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.today.dayOfWeek.value == 7) ColorSundayLight else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.today.dayOfMonth}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    lineHeight = 68.sp
                )
                Text(
                    text = "Tháng ${state.today.monthValue}, ${state.today.year}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bên phải: Khối bo góc Âm lịch & Can Chi màu Pastel Vàng
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF2C2417) else Color(0xFFFFF2D9))
                    .border(1.dp, if (isDark) Color(0xFF534125) else Color(0xFFFDE68A), RoundedCornerShape(16.dp))
                    .padding(vertical = 20.dp, horizontal = 12.dp),
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
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Âm lịch",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "(${state.lunarDate.canChiDay})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF78350F)
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
    val isDark = isSystemInDarkTheme()
    val isHoangDao = state.auspicious.isHoangDao

    val bg = if (isDark) {
        if (isHoangDao) Color(0xFF172D22) else Color(0xFF33231A)
    } else {
        if (isHoangDao) Color(0xFFDEF7EC) else Color(0xFFFFE4CE)
    }

    val borderCol = if (isDark) {
        if (isHoangDao) Color(0xFF1E4833) else Color(0xFF5A3926)
    } else {
        if (isHoangDao) Color(0xFFA7F3D0) else Color(0xFFFED7AA)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isHoangDao) Color(0xFF059669) else Color(0xFFEA580C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHoangDao) "Ngày Hoàng Đạo (Cát Lành)" else "Ngày Hắc Đạo (Cẩn Trọng)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else (if (isHoangDao) Color(0xFF065F46) else Color(0xFF9A3412))
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val hoursText = if (state.auspicious.gioHoangDao.isNotEmpty()) {
                "Giờ tốt: " + state.auspicious.gioHoangDao.joinToString(", ")
            } else {
                "Giờ tốt: Tí (23-1), Sửu (1-3), Mão (5-7), Ngọ (11-13)"
            }
            Text(
                text = hoursText,
                fontSize = 13.5.sp,
                color = if (isDark) Color.White.copy(alpha = 0.8f) else (if (isHoangDao) Color(0xFF047857) else Color(0xFFC2410C)),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun SpecialEventsTodayCard(
    state: TodayUiState,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF1E1E1E) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
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
                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
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
    tasks: List<Task>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTaskToggle: (Long, Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF1E1E1E) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.today_schedule_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tasks.isEmpty()) {
                        Text(
                            text = "Chưa có lịch trình cho hôm nay. Nhấn nút + màu vàng để thêm!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        tasks.forEach { task ->
                            TaskItemCard(
                                task = task,
                                onToggle = { isDone -> onTaskToggle(task.id, isDone) },
                                onEdit = {},
                                onDelete = {}
                            )
                        }
                    }
                }
            }
        }
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
        shape = RoundedCornerShape(18.dp),
        title = { Text(text = "Thêm lịch trình hôm nay", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên công việc") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Kết thúc (11:30)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Địa điểm / Ghi chú") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                enabled = title.isNotBlank()
            ) {
                Text("Lưu lại", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
