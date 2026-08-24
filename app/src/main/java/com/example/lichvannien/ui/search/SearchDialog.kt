package com.example.lichvannien.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.theme.AppHeaderBlue
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onNavigateToDay: (year: Int, month: Int, day: Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Search Bar
                Surface(
                    color = AppHeaderBlue,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Đóng tìm kiếm",
                                tint = Color.White
                            )
                        }

                        TextField(
                            value = state.query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Tìm ngày lễ, sự kiện, công việc...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (state.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Xóa",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (state.query.isBlank()) {
                        // 1. Gợi ý ngày lễ phổ biến
                        item(key = "popular_holidays_header") {
                            Text(
                                text = "Gợi ý ngày lễ tiêu biểu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item(key = "popular_holidays_chips") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.popularHolidays, key = { it.name }) { holiday ->
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.onQueryChanged(holiday.name)
                                        },
                                        label = {
                                            Text(text = "${holiday.icon ?: "🎉"} ${holiday.name}")
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }

                        // 2. Tra cứu chuyển đổi Ngày Dương ↔ Ngày Âm
                        item(key = "quick_lookup_card") {
                            QuickDateLookupCard(
                                solarDate = state.quickDateSolar,
                                lunarDate = state.quickDateLunar,
                                auspicious = state.quickDateAuspicious,
                                onLookupSolar = { y, m, d -> viewModel.lookupSolarDate(y, m, d) },
                                onNavigateToDetail = { y, m, d ->
                                    onDismiss()
                                    onNavigateToDay(y, m, d)
                                }
                            )
                        }
                    } else {
                        // KHI CÓ KẾT QUẢ TÌM KIẾM
                        val hasNoResults = state.specialDays.isEmpty() && state.tasks.isEmpty() && state.quickDateSolar == null

                        if (hasNoResults) {
                            item(key = "empty_result") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Không tìm thấy kết quả nào phù hợp",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        // Danh sách ngày lễ tìm thấy
                        if (state.specialDays.isNotEmpty()) {
                            item(key = "special_days_section") {
                                Text(
                                    text = "Ngày lễ & Sự kiện (${state.specialDays.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(state.specialDays, key = { "special_${it.name}_${it.solarDay}_${it.lunarDay}" }) { day ->
                                SpecialDaySearchResultItem(
                                    specialDay = day,
                                    onClick = {
                                        val currentYear = LocalDate.now().year
                                        val targetSolar = if (day.isLunar && day.lunarMonth != null && day.lunarDay != null) {
                                            com.example.lichvannien.domain.util.LunarConverter.lunarToSolar(
                                                currentYear,
                                                day.lunarMonth,
                                                day.lunarDay,
                                                day.leapMonth
                                            )
                                        } else {
                                            com.example.lichvannien.domain.model.SolarDate(
                                                currentYear,
                                                day.solarMonth ?: 1,
                                                day.solarDay ?: 1
                                            )
                                        }
                                        onDismiss()
                                        onNavigateToDay(targetSolar.year, targetSolar.month, targetSolar.day)
                                    }
                                )
                            }
                        }

                        // Danh sách công việc / Task tìm thấy
                        if (state.tasks.isNotEmpty()) {
                            item(key = "tasks_section") {
                                Text(
                                    text = "Lịch trình & Việc cần làm (${state.tasks.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(state.tasks, key = { "task_${it.id}" }) { task ->
                                com.example.lichvannien.ui.task.components.TaskItemCard(
                                    task = task,
                                    onToggle = {},
                                    onEdit = {
                                        val parts = task.date.split("-")
                                        if (parts.size == 3) {
                                            val y = parts[0].toIntOrNull() ?: LocalDate.now().year
                                            val m = parts[1].toIntOrNull() ?: LocalDate.now().monthValue
                                            val d = parts[2].toIntOrNull() ?: LocalDate.now().dayOfMonth
                                            onDismiss()
                                            onNavigateToDay(y, m, d)
                                        }
                                    },
                                    onDelete = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickDateLookupCard(
    solarDate: com.example.lichvannien.domain.model.SolarDate?,
    lunarDate: com.example.lichvannien.domain.model.LunarDate?,
    auspicious: com.example.lichvannien.domain.model.AuspiciousResult?,
    onLookupSolar: (year: Int, month: Int, day: Int) -> Unit,
    onNavigateToDetail: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableIntStateOf(LocalDate.now().year) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AppHeaderBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tra cứu & Đổi ngày Âm - Dương",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Quick Jump Inputs (Day / Month / Year)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedDay.toString(),
                    onValueChange = {
                        val d = it.toIntOrNull()
                        if (d != null && d in 1..31) {
                            selectedDay = d
                            onLookupSolar(selectedYear, selectedMonth, d)
                        }
                    },
                    label = { Text("Ngày") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = selectedMonth.toString(),
                    onValueChange = {
                        val m = it.toIntOrNull()
                        if (m != null && m in 1..12) {
                            selectedMonth = m
                            onLookupSolar(selectedYear, m, selectedDay)
                        }
                    },
                    label = { Text("Tháng") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = selectedYear.toString(),
                    onValueChange = {
                        val y = it.toIntOrNull()
                        if (y != null && y in 1900..2100) {
                            selectedYear = y
                            onLookupSolar(y, selectedMonth, selectedDay)
                        }
                    },
                    label = { Text("Năm") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
            }

            // Converted Results Preview
            if (solarDate != null && lunarDate != null && auspicious != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dương lịch: ${solarDate.day}/${solarDate.month}/${solarDate.year}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = if (auspicious.isHoangDao) Color(0xFF2E7D32) else Color(0xFFC62828),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (auspicious.isHoangDao) "Hoàng Đạo" else "Hắc Đạo",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Âm lịch: Ngày ${lunarDate.day} tháng ${lunarDate.month} (Năm ${lunarDate.canChiYear})",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ngày: ${lunarDate.canChiDay}, Trực: ${auspicious.truc}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { onNavigateToDetail(solarDate.year, solarDate.month, solarDate.day) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xem chi tiết ngày ${solarDate.day}/${solarDate.month}/${solarDate.year}")
                }
            }
        }
    }
}

@Composable
private fun SpecialDaySearchResultItem(
    specialDay: SpecialDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = specialDay.icon ?: "🎉",
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = specialDay.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val dateText = if (specialDay.isLunar) {
                    "Âm lịch: ${specialDay.lunarDay}/${specialDay.lunarMonth}"
                } else {
                    "Dương lịch: ${specialDay.solarDay}/${specialDay.solarMonth}"
                }
                Text(
                    text = dateText,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = AppHeaderBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
