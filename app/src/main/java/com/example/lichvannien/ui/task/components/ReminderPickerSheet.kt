package com.example.lichvannien.ui.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper
import com.example.lichvannien.ui.task.util.TaskReminderMode
import com.example.lichvannien.ui.task.util.TaskRepeatRule
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerSheet(
    initialDate: String?,
    initialDueTime: String?,
    initialRepeat: String,
    initialReminderType: String,
    onDismiss: () -> Unit,
    onApply: (date: String, dueTime: String?, repeat: String, reminderType: String) -> Unit,
    onClear: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedDate by remember {
        mutableStateOf(
            TaskDateTimeHelper.parseDate(initialDate) ?: LocalDate.now()
        )
    }

    var selectedTime by remember {
        mutableStateOf(
            TaskDateTimeHelper.parseTime(initialDueTime) ?: LocalTime.of(8, 0)
        )
    }

    var isTimeEnabled by remember {
        mutableStateOf(initialDueTime != null)
    }

    var selectedRepeat by remember {
        mutableStateOf(TaskRepeatRule.fromCode(initialRepeat))
    }

    var selectedReminderMode by remember {
        mutableStateOf(TaskReminderMode.fromCode(initialReminderType))
    }

    var showTimePickerModal by remember { mutableStateOf(false) }
    var showDatePickerModal by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    // TimePicker Dialog
    if (showTimePickerModal) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePickerModal = false },
            title = { Text("Chọn giờ hẹn", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        isTimeEnabled = true
                        showTimePickerModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue)
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerModal = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // DatePicker Dialog
    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePickerModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue)
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cài đặt lời nhắc & lặp lại",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (initialDueTime != null || initialDate != null) {
                    TextButton(onClick = onClear) {
                        Text("Xóa nhắc nhở", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            // 1. Mục chọn Ngày
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AppHeaderBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ngày thực hiện", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDate == today,
                        onClick = { selectedDate = today },
                        label = { Text("Hôm nay") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedDate == tomorrow,
                        onClick = { selectedDate = tomorrow },
                        label = { Text("Ngày mai") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedDate != today && selectedDate != tomorrow,
                        onClick = { showDatePickerModal = true },
                        label = {
                            Text(
                                if (selectedDate != today && selectedDate != tomorrow) {
                                    "${selectedDate.dayOfMonth}/${selectedDate.monthValue}"
                                } else "Tự chọn"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Mục chọn Giờ
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = AppHeaderBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Giờ hẹn", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Switch(
                        checked = isTimeEnabled,
                        onCheckedChange = {
                            isTimeEnabled = it
                            if (it && !showTimePickerModal) {
                                // Mở time picker nếu chưa có giờ
                            }
                        }
                    )
                }

                if (isTimeEnabled) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePickerModal = true },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thời gian nhắc nhở",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val timeStr = "${selectedTime.hour}:${if (selectedTime.minute < 10) "0${selectedTime.minute}" else "${selectedTime.minute}"}"
                            Surface(
                                color = AppHeaderBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = timeStr,
                                    color = AppHeaderBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Tần suất lặp lại (Recurrence)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = AppHeaderBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tần suất lặp lại", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rules = TaskRepeatRule.entries
                    // Chia thành các dòng để hiển thị rõ ràng
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.ONCE,
                            onClick = { selectedRepeat = TaskRepeatRule.ONCE },
                            label = { Text("Chỉ một lần") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.DAILY,
                            onClick = { selectedRepeat = TaskRepeatRule.DAILY },
                            label = { Text("Hàng ngày") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.WEEKDAYS,
                            onClick = { selectedRepeat = TaskRepeatRule.WEEKDAYS },
                            label = { Text("T2 đến T6") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.WEEKLY,
                            onClick = { selectedRepeat = TaskRepeatRule.WEEKLY },
                            label = { Text("Hàng tuần") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.MONTHLY,
                            onClick = { selectedRepeat = TaskRepeatRule.MONTHLY },
                            label = { Text("Hàng tháng") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRepeat == TaskRepeatRule.YEARLY,
                            onClick = { selectedRepeat = TaskRepeatRule.YEARLY },
                            label = { Text("Hàng năm") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Chế độ lời nhắc (Reminder Mode)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppHeaderBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kiểu nhắc nhở", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedReminderMode = TaskReminderMode.NOTIFICATION },
                        color = if (selectedReminderMode == TaskReminderMode.NOTIFICATION) {
                            AppHeaderBlue.copy(alpha = 0.12f)
                        } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selectedReminderMode == TaskReminderMode.NOTIFICATION) AppHeaderBlue else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (selectedReminderMode == TaskReminderMode.NOTIFICATION) AppHeaderBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thông báo",
                                fontWeight = if (selectedReminderMode == TaskReminderMode.NOTIFICATION) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.5.sp,
                                color = if (selectedReminderMode == TaskReminderMode.NOTIFICATION) AppHeaderBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedReminderMode = TaskReminderMode.ALARM },
                        color = if (selectedReminderMode == TaskReminderMode.ALARM) {
                            Color(0xFFE65100).copy(alpha = 0.12f)
                        } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selectedReminderMode == TaskReminderMode.ALARM) Color(0xFFE65100) else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (selectedReminderMode == TaskReminderMode.ALARM) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Báo thức",
                                fontWeight = if (selectedReminderMode == TaskReminderMode.ALARM) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.5.sp,
                                color = if (selectedReminderMode == TaskReminderMode.ALARM) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hủy")
                }

                Button(
                    onClick = {
                        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val dueTimeStr = if (isTimeEnabled) {
                            val h = if (selectedTime.hour < 10) "0${selectedTime.hour}" else "${selectedTime.hour}"
                            val m = if (selectedTime.minute < 10) "0${selectedTime.minute}" else "${selectedTime.minute}"
                            "$h:$m"
                        } else null

                        onApply(
                            dateStr,
                            dueTimeStr,
                            selectedRepeat.code,
                            selectedReminderMode.code
                        )
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue)
                ) {
                    Text("Áp dụng", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
