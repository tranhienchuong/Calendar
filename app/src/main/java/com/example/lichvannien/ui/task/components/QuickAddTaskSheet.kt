package com.example.lichvannien.ui.task.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.ui.task.model.TaskPastelColor
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTaskSheet(
    onDismiss: () -> Unit,
    onAddTask: (
        title: String,
        colorTag: String,
        date: String,
        dueTime: String?,
        repeatType: String,
        reminderType: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val isDark = isSystemInDarkTheme()

    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(TaskPastelColor.YELLOW) }

    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    var date by remember { mutableStateOf(todayStr) }
    var dueTime by remember { mutableStateOf<String?>(null) }
    var repeatType by remember { mutableStateOf("DAILY") }
    var reminderType by remember { mutableStateOf("NOTIFICATION") }

    var showReminderPicker by remember { mutableStateOf(false) }
    var showColorPalette by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    fun submitCurrentTask() {
        if (title.isNotBlank()) {
            onAddTask(
                title.trim(),
                selectedColor.tag,
                date,
                dueTime,
                repeatType,
                reminderType
            )
            title = ""
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Input TextField
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "Vào việc cần làm",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        submitCurrentTask()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor.accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // Gợi ý nhấn Enter để tạo việc liên tục
            Text(
                text = "💡 Nhấn Enter để tạo việc cần làm khác",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Reminder active badge nếu có
            if (dueTime != null || date != todayStr) {
                Surface(
                    color = AppHeaderBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { showReminderPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = AppHeaderBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val formattedSubtitle = remember(date, dueTime, repeatType) {
                            val dummyTask = com.example.lichvannien.data.local.entity.TaskEntity(
                                title = "",
                                date = date,
                                dueTime = dueTime,
                                repeatType = repeatType
                            )
                            TaskDateTimeHelper.formatTaskSubtitle(dummyTask)
                        }
                        Text(
                            text = formattedSubtitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppHeaderBlue
                        )
                    }
                }
            }

            // Bảng chọn màu sắc Pastel
            AnimatedVisibility(visible = showColorPalette) {
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

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )

            // Toolbar phụ & Nút Xong
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Bảng màu
                    IconButton(
                        onClick = { showColorPalette = !showColorPalette }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Chọn màu",
                            tint = if (showColorPalette) selectedColor.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Nút Chuông báo
                    IconButton(
                        onClick = { showReminderPicker = true }
                    ) {
                        Icon(
                            imageVector = if (dueTime != null) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Cài đặt lời nhắc",
                            tint = if (dueTime != null) AppHeaderBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Nút "Xong" để lưu và đóng
                Button(
                    onClick = {
                        submitCurrentTask()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor.accentColor)
                ) {
                    Text(
                        text = "Xong",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
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
                date = todayStr
                dueTime = null
                repeatType = "DAILY"
                reminderType = "NOTIFICATION"
                showReminderPicker = false
            }
        )
    }
}
