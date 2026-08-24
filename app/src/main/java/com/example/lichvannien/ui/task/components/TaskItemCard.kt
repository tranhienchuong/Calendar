package com.example.lichvannien.ui.task.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.ui.task.model.TaskPastelColor
import com.example.lichvannien.ui.task.util.TaskDateTimeHelper

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val pastelColor = remember(task.colorTag, task.colorHex) {
        if (!task.colorTag.isNullOrBlank() && task.colorTag != "DEFAULT") {
            TaskPastelColor.fromTag(task.colorTag)
        } else {
            TaskPastelColor.fromHex(task.colorHex)
        }
    }

    val cardBg = if (isDark) pastelColor.darkBg else pastelColor.lightBg
    val cardBorder = if (isDark) pastelColor.darkBorder else pastelColor.lightBorder

    val isOverdue = remember(task.date, task.dueTime, task.startTime, task.isCompleted) {
        TaskDateTimeHelper.isOverdue(task)
    }

    val subtitle = remember(task) {
        TaskDateTimeHelper.formatTaskSubtitle(task)
    }

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onToggle(!task.isCompleted) },
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder.copy(alpha = 0.8f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox tròn phong cách Minimalist
            PastelCheckCircle(
                checked = task.isCompleted,
                accentColor = pastelColor.accentColor,
                onCheckedChange = onToggle
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Nội dung chính: Tên công việc & Dòng thông tin phụ
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) {
                        if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF1F2937).copy(alpha = 0.45f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF1F2937)
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = if (isOverdue && !task.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            task.isCompleted -> if (isDark) Color.White.copy(alpha = 0.35f) else Color(0xFF6B7280).copy(alpha = 0.5f)
                            isOverdue -> Color(0xFFDC2626) // Màu đỏ cảnh báo quá hạn
                            else -> if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF4B5563)
                        }
                    )
                }
            }

            // Menu tùy chọn (Sửa / Xóa)
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF6B7280).copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa công việc", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PastelCheckCircle(
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1f,
        label = "check_scale"
    )

    val borderColor = if (checked) {
        accentColor
    } else {
        if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF4B5563).copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (checked) accentColor else Color.Transparent
            )
            .border(
                width = if (checked) 0.dp else 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Hoàn thành",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
