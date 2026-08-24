package com.example.lichvannien.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.theme.*
import com.example.lichvannien.ui.today.AddTodayTaskDialog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

private const val BASE_YEAR = 2000
private const val TOTAL_PAGES = 2400 // Covers years 2000 to 2200

private fun pageFromYearMonth(yearMonth: YearMonth): Int {
    return (yearMonth.year - BASE_YEAR) * 12 + (yearMonth.monthValue - 1)
}

private fun yearMonthFromPage(page: Int): YearMonth {
    val year = BASE_YEAR + page / 12
    val month = (page % 12) + 1
    return YearMonth.of(year, month)
}

@Composable
fun CalendarScreen(
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val initialPage = remember {
        val now = YearMonth.now()
        pageFromYearMonth(now)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { TOTAL_PAGES })

    // Sync ViewModel preload when page settles
    LaunchedEffect(pagerState.currentPage) {
        val settledMonth = yearMonthFromPage(pagerState.currentPage)
        viewModel.loadMonth(settledMonth.year, settledMonth.monthValue)
    }

    val fabColor = Color(0xFFFBBF24)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = fabColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = fabColor, spotColor = fabColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm sự kiện",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Month Header: "THÁNG 8, 2026" with < and > arrows
            CalendarHeader(
                pagerState = pagerState,
                onPreviousClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNextClick = {
                    if (pagerState.currentPage < TOTAL_PAGES - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )

            // 2. Weekdays Header Row: T2  T3  T4  T5  T6  T7  CN
            WeekdaysHeader()

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            // 3. Grid HorizontalPager (Month table)
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.35f)
            ) { page ->
                MonthPage(
                    page = page,
                    uiState = uiState,
                    viewModel = viewModel,
                    selectedDate = uiState.selectedDate,
                    onDayClick = { y, m, d ->
                        viewModel.selectDate(LocalDate.of(y, m, d))
                        onDayClick(y, m, d)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Bottom Schedule Panel (Matching Image 1)
            SelectedDateScheduleCard(
                selectedDate = uiState.selectedDate,
                tasks = uiState.selectedDateTasks,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            )
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
fun CalendarHeader(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMonth by remember(pagerState) {
        derivedStateOf {
            if (pagerState.isScrollInProgress) {
                yearMonthFromPage(pagerState.targetPage)
            } else {
                yearMonthFromPage(pagerState.currentPage)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "THÁNG ${activeMonth.monthValue}, ${activeMonth.year}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeekdaysHeader(
    modifier: Modifier = Modifier
) {
    val weekdays = listOf(
        R.string.day_mon to (MaterialTheme.colorScheme.onSurface to FontWeight.Bold),
        R.string.day_tue to (MaterialTheme.colorScheme.onSurface to FontWeight.Bold),
        R.string.day_wed to (MaterialTheme.colorScheme.onSurface to FontWeight.Bold),
        R.string.day_thu to (MaterialTheme.colorScheme.onSurface to FontWeight.Bold),
        R.string.day_fri to (MaterialTheme.colorScheme.onSurface to FontWeight.Bold),
        R.string.day_sat to (Color(0xFF8B0000) to FontWeight.Bold),
        R.string.day_sun to (Color(0xFFC62828) to FontWeight.Bold)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        weekdays.forEach { (weekdayId, style) ->
            val (color, fontWeight) = style
            Text(
                text = stringResource(weekdayId),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = fontWeight,
                fontSize = 13.5.sp,
                color = color
            )
        }
    }
}

@Composable
fun MonthPage(
    page: Int,
    uiState: CalendarUiState,
    viewModel: CalendarViewModel,
    selectedDate: LocalDate,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pageMonth = remember(page) { yearMonthFromPage(page) }
    val days = uiState.monthDataMap[pageMonth] ?: viewModel.getMonthDays(pageMonth) ?: persistentListOf()

    CalendarGrid(
        days = days,
        selectedDate = selectedDate,
        onDayClick = onDayClick,
        modifier = modifier
    )
}

@Composable
fun CalendarGrid(
    days: ImmutableList<CalendarDayUiModel>,
    selectedDate: LocalDate,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .border(0.5.dp, borderColor)
    ) {
        for (rowIndex in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (colIndex in 0 until 7) {
                    val dayIndex = rowIndex * 7 + colIndex
                    if (dayIndex < days.size) {
                        val day = days[dayIndex]
                        val isSelected = day.isCurrentMonth &&
                                day.year == selectedDate.year &&
                                day.month == selectedDate.monthValue &&
                                day.day == selectedDate.dayOfMonth

                        key(day.year, day.month, day.day) {
                            DayCell(
                                day = day,
                                isSelected = isSelected,
                                borderColor = borderColor,
                                onDayClick = onDayClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, borderColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDayUiModel,
    isSelected: Boolean,
    borderColor: Color,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellModifier = modifier
        .fillMaxHeight()
        .border(0.5.dp, borderColor)
        .clickable(
            enabled = day.isCurrentMonth,
            onClick = { onDayClick(day.year, day.month, day.day) }
        )
        .padding(horizontal = 1.dp, vertical = 2.dp)
        .clearAndSetSemantics {
            contentDescription = day.contentDescription
        }

    if (!day.isCurrentMonth) {
        // Empty cell for days outside the month
        Box(modifier = cellModifier)
        return
    }

    val isDark = isSystemInDarkTheme()
    val solarDayColor = when {
        day.isSunday -> Color(0xFFC62828)
        day.isSaturday -> Color(0xFF8B0000)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (day.isToday) {
            // Ngày Hôm Nay: LUÔN LUÔN hiển thị hình tròn màu vàng cam đặc trưng
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B))
                    .then(
                        if (isSelected) Modifier.border(1.5.dp, if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.solarDayText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (day.lunarDayText != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = day.lunarDayText,
                    color = Color(0xFFD97706),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else if (isSelected) {
            // Ngày được chọn khác (không phải hôm nay): Viền tròn màu vàng cam + nền pastel nhẹ
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF2C2417) else Color(0xFFFEF3C7))
                    .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.solarDayText,
                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (day.lunarDayText != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = day.lunarDayText,
                    color = Color(0xFFD97706),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Ngày bình thường
            Text(
                text = day.solarDayText,
                color = solarDayColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (day.lunarDayText != null) {
                val hasSubLabel = day.lunarSubLabel != null
                Text(
                    text = day.lunarDayText,
                    color = if (hasSubLabel) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (hasSubLabel) FontWeight.SemiBold else FontWeight.Normal
                )

                if (day.lunarSubLabel != null) {
                    Text(
                        text = day.lunarSubLabel,
                        color = Color(0xFFDC2626),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedDateScheduleCard(
    selectedDate: LocalDate,
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val isToday = selectedDate == LocalDate.now()
    val dateHeaderTitle = if (isToday) {
        "Hôm nay, ${selectedDate.dayOfMonth} Thg ${selectedDate.monthValue}, ${selectedDate.year}"
    } else {
        val dow = when (selectedDate.dayOfWeek.value) {
            1 -> "Thứ Hai"
            2 -> "Thứ Ba"
            3 -> "Thứ Tư"
            4 -> "Thứ Năm"
            5 -> "Thứ Sáu"
            6 -> "Thứ Bảy"
            else -> "Chủ Nhật"
        }
        "$dow, ${selectedDate.dayOfMonth} Thg ${selectedDate.monthValue}, ${selectedDate.year}"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Drag handle pill at top
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = dateHeaderTitle,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có lịch trình cho ngày này.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks.size, key = { tasks[it].id }) { index ->
                        val task = tasks[index]
                        com.example.lichvannien.ui.task.components.TaskItemCard(
                            task = task,
                            onToggle = {},
                            onEdit = {},
                            onDelete = {}
                        )
                    }
                }
            }
        }
    }
}
