package com.example.lichvannien.ui.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.ui.task.model.TaskPastelColor
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    task: Task,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()

    var title by remember { mutableStateOf(task.title) }
    var selectedColor by remember {
        mutableStateOf(
            if (!task.colorTag.isNullOrBlank() && task.colorTag != "DEFAULT") {
                TaskPastelColor.fromTag(task.colorTag)
            } else {
                TaskPastelColor.fromHex(task.colorHex)
            }
        )
    }

    var date by remember { mutableStateOf(task.date) }
    var dueTime by remember { mutableStateOf(task.dueTime ?: task.startTime) }
    var repeatType by remember { mutableStateOf(task.repeatType) }
    var reminderType by remember { mutableStateOf(task.reminderType) }

    var showReminderPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Chỉnh sửa công việc",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Input TextField
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên công việc") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor.accentColor
                )
            )

            // Reminder settings button / chip
            Surface(
                color = if (dueTime != null) AppHeaderBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReminderPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (dueTime != null) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (dueTime != null) AppHeaderBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val formattedSubtitle = remember(date, dueTime, repeatType) {
                            val dummyTask = task.copy(date = date, dueTime = dueTime, repeatType = repeatType)
                            val subtitle = TaskDateTimeHelper.formatTaskSubtitle(dummyTask)
                            if (subtitle.isBlank()) "Đặt giờ hẹn & Lặp lại" else subtitle
                        }
                        Text(
                            text = formattedSubtitle,
                            fontSize = 14.sp,
                            fontWeight = if (dueTime != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (dueTime != null) AppHeaderBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bảng chọn màu sắc Pastel
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Màu thẻ công việc",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskPastelColor.entries.forEach { pastel ->
                        val isSelected = selectedColor == pastel
                        val circleBg = if (isDark) pastel.darkBg else pastel.lightBg
                        val borderCol = if (isSelected) pastel.accentColor else (if (isDark) pastel.darkBorder else pastel.lightBorder)

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = borderCol,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = pastel },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = pastel.displayName,
                                    tint = pastel.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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
                        if (title.isNotBlank()) {
                            onSaveTask(
                                task.copy(
                                    title = title.trim(),
                                    colorTag = selectedColor.tag,
                                    colorHex = selectedColor.colorHex,
                                    date = date,
                                    dueTime = dueTime,
                                    startTime = dueTime,
                                    repeatType = repeatType,
                                    reminderType = reminderType
                                )
                            )
                            onDismiss()
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor.accentColor)
                ) {
                    Text("Lưu thay đổi", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (showReminderPicker) {
        ReminderPickerSheet(
            initialDate = date,
            initialDueTime = dueTime,
            initialRepeat = repeatType,
            initialReminderType = reminderType,
            onDismiss = { showReminderPicker = false },
            onApply = { newDate, newDueTime, newRepeat, newReminderType ->
                date = newDate
                dueTime = newDueTime
                repeatType = newRepeat
                reminderType = newReminderType
                showReminderPicker = false
            },
            onClear = {
                dueTime = null
                repeatType = "DAILY"
                reminderType = "NOTIFICATION"
                showReminderPicker = false
            }
        )
    }
}
