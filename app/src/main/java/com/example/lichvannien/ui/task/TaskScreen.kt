package com.example.lichvannien.ui.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.ui.task.components.EditTaskSheet
import com.example.lichvannien.ui.task.components.QuickAddTaskSheet
import com.example.lichvannien.ui.task.components.TaskItemCard

@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSortMenu by remember { mutableStateOf(false) }

    val fabColor = Color(0xFFFBBF24) // Màu vàng hổ phách nổi bật chuẩn thiết kế

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = fabColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 4.dp)
                    .size(58.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = fabColor, spotColor = fabColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm công việc",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        },
        containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF9FAFB),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Header (Icon vuông bo góc đen-vàng + Tiêu đề + Đếm số lượng + Actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Badge biểu tượng App / Task (nền đen bo góc + dấu tích vàng)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF262626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFBBF24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Việc cần làm",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF111827),
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "${state.pendingCount} việc cần làm",
                            fontSize = 13.5.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7280),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Nút Tìm kiếm & Nút Lọc/Sắp xếp
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = if (isSearchActive) AppHeaderBlue else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF374151)),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showFilterSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Lọc & Sắp xếp",
                                tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF374151),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterSortMenu,
                            onDismissRequest = { showFilterSortMenu = false }
                        ) {
                            Text(
                                text = "  LỌC DANH SÁCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            TaskFilter.entries.forEach { f ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = f.label,
                                            fontWeight = if (state.filter == f) FontWeight.Bold else FontWeight.Normal,
                                            color = if (state.filter == f) AppHeaderBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = if (state.filter == f) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppHeaderBlue, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    onClick = {
                                        viewModel.setFilter(f)
                                        showFilterSortMenu = false
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = "  SẮP XẾP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            TaskSort.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = s.label,
                                            fontWeight = if (state.sort == s) FontWeight.Bold else FontWeight.Normal,
                                            color = if (state.sort == s) AppHeaderBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = if (state.sort == s) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppHeaderBlue, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    onClick = {
                                        viewModel.setSort(s)
                                        showFilterSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Search Bar mở rộng nếu đang active
            AnimatedVisibility(visible = isSearchActive) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Tìm kiếm công việc...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Thanh Filter Chips ngang tiện lợi
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TaskFilter.entries) { filterItem ->
                    val isSelected = state.filter == filterItem
                    val countBadge = when (filterItem) {
                        TaskFilter.ALL -> state.totalCount
                        TaskFilter.PENDING -> state.pendingCount
                        TaskFilter.COMPLETED -> state.completedCount
                        TaskFilter.TODAY -> state.allTasks.count { it.date == java.time.LocalDate.now().toString() }
                        TaskFilter.OVERDUE -> state.overdueCount
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filterItem) },
                        label = {
                            Text(
                                text = "${filterItem.label} ($countBadge)",
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) Color(0xFF2E384D) else Color(0xFFE2E8F0),
                            selectedLabelColor = if (isDark) Color.White else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Danh sách thẻ công việc Pastel (Task List)
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFBBF24))
                }
            } else if (state.displayedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 42.sp
                        )
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "Không tìm thấy công việc phù hợp" else "Tất cả công việc đã hoàn thành!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF374151)
                        )
                        Text(
                            text = "Nhấn nút + màu vàng để thêm việc cần làm mới",
                            fontSize = 13.5.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF9CA3AF)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.displayedTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { isDone -> viewModel.toggleTask(task.id, isDone) },
                            onEdit = { taskToEdit = task },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }
    }

    // Modal tạo nhanh công việc
    if (showQuickAddSheet) {
        QuickAddTaskSheet(
            onDismiss = { showQuickAddSheet = false },
            onAddTask = { title, colorTag, date, dueTime, repeatType, reminderType ->
                viewModel.addTask(
                    title = title,
                    date = date,
                    colorTag = colorTag,
                    dueTime = dueTime,
                    repeatType = repeatType,
                    reminderType = reminderType
                )
            }
        )
    }

    // Modal chỉnh sửa công việc
    taskToEdit?.let { task ->
        EditTaskSheet(
            task = task,
            onDismiss = { taskToEdit = null },
            onSaveTask = { updatedTask ->
                viewModel.updateTask(updatedTask)
                taskToEdit = null
            }
        )
    }
}
