package com.example.lichvannien.ui.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.theme.*
import com.example.lichvannien.domain.model.CalendarDay
import java.time.YearMonth
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val daysList by viewModel.daysList.collectAsStateWithLifecycle()

    var direction by remember { mutableStateOf(1) } // 1: next, -1: prev

    var dragAmountX by remember { mutableStateOf(0f) }
    var isSwipeTriggered by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = {
                    direction = -1
                    viewModel.goToPreviousMonth()
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }

                Text(
                    text = stringResource(
                        R.string.calendar_header_format,
                        currentMonthYear.monthValue,
                        currentMonthYear.year
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = {
                    direction = 1
                    viewModel.goToNextMonth()
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            FilledTonalButton(
                onClick = {
                    val now = YearMonth.now()
                    direction = if (now.isAfter(currentMonthYear)) 1 else -1
                    viewModel.goToToday()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_today),
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid wrapped in a beautiful modern ElevatedCard with Swipe Gesture
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(currentMonthYear) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragAmountX = 0f
                            isSwipeTriggered = false
                        },
                        onDragEnd = {
                            dragAmountX = 0f
                            isSwipeTriggered = false
                        },
                        onDragCancel = {
                            dragAmountX = 0f
                            isSwipeTriggered = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!isSwipeTriggered) {
                                dragAmountX += dragAmount
                                if (dragAmountX > 100f) { // Swipe right -> previous month
                                    isSwipeTriggered = true
                                    direction = -1
                                    viewModel.goToPreviousMonth()
                                } else if (dragAmountX < -100f) { // Swipe left -> next month
                                    isSwipeTriggered = true
                                    direction = 1
                                    viewModel.goToNextMonth()
                                }
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Weekdays Header Grid
                val isDark = isSystemInDarkTheme()
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                val weekdays = remember(isDark, onSurfaceVariant) {
                    listOf(
                        R.string.day_mon to (onSurfaceVariant to FontWeight.SemiBold),
                        R.string.day_tue to (onSurfaceVariant to FontWeight.SemiBold),
                        R.string.day_wed to (onSurfaceVariant to FontWeight.SemiBold),
                        R.string.day_thu to (onSurfaceVariant to FontWeight.SemiBold),
                        R.string.day_fri to (onSurfaceVariant to FontWeight.SemiBold),
                        R.string.day_sat to ((if (isDark) ColorSaturdayDark else ColorSaturdayLight) to FontWeight.Bold),
                        R.string.day_sun to ((if (isDark) ColorSundayDark else ColorSundayLight) to FontWeight.Bold)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    weekdays.forEach { (weekdayId, style) ->
                        val (color, fontWeight) = style
                        Text(
                            text = stringResource(weekdayId),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = fontWeight,
                            fontSize = 14.sp,
                            color = color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid with horizontal slide animations
                AnimatedContent(
                    targetState = daysList,
                    transitionSpec = {
                        if (direction > 0) {
                            (slideInHorizontally { width -> width } + fadeIn() ) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn() ) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { targetDays ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = targetDays,
                            key = { day -> "${day.solarDate.year}-${day.solarDate.month}-${day.solarDate.day}" }
                        ) { day ->
                            DayCell(
                                day = day,
                                onClick = {
                                    onDayClick(day.solarDate.year, day.solarDate.month, day.solarDate.day)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    // Determine cell day of the week
    val localDate = java.time.LocalDate.of(day.solarDate.year, day.solarDate.month, day.solarDate.day)
    val dayOfWeek = localDate.dayOfWeek
    val isSaturday = dayOfWeek == java.time.DayOfWeek.SATURDAY
    val isSunday = dayOfWeek == java.time.DayOfWeek.SUNDAY

    // Background color (Today has highest priority, using primary color scheme)
    val containerColor = when {
        !day.isCurrentMonth -> Color.Transparent
        day.isToday -> MaterialTheme.colorScheme.primary
        day.isHoangDao -> if (isDark) ColorHoangDaoBgDark else ColorHoangDaoBgLight
        else -> if (isDark) ColorHacDaoBgDark else ColorHacDaoBgLight // Transparent
    }

    // Solar day text color (Today has onPrimary color)
    val solarDayColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        day.isToday -> MaterialTheme.colorScheme.onPrimary
        isSaturday -> if (isDark) ColorSaturdayDark else ColorSaturdayLight
        isSunday -> if (isDark) ColorSundayDark else ColorSundayLight
        day.isHoangDao -> if (isDark) ColorHoangDaoTextDark else ColorHoangDaoTextLight
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Lunar day text color (Today has onPrimary with opacity)
    val lunarDayColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        day.isToday -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        day.isHoangDao -> if (isDark) ColorHoangDaoTextDark else ColorHoangDaoTextLight
        isSaturday -> (if (isDark) ColorSaturdayDark else ColorSaturdayLight).copy(alpha = 0.8f)
        isSunday -> (if (isDark) ColorSundayDark else ColorSundayLight).copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val solarDayFontWeight = when {
        day.isToday -> FontWeight.Bold
        isSaturday || isSunday -> FontWeight.Bold
        else -> FontWeight.Medium
    }

    // Build accessibility description for TalkBack
    val accessibilityDescription = buildString {
        if (day.isToday) {
            append("Hôm nay, ")
        }
        val dayOfWeekName = when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Thứ Hai"
            java.time.DayOfWeek.TUESDAY -> "Thứ Ba"
            java.time.DayOfWeek.WEDNESDAY -> "Thứ Tư"
            java.time.DayOfWeek.THURSDAY -> "Thứ Năm"
            java.time.DayOfWeek.FRIDAY -> "Thứ Sáu"
            java.time.DayOfWeek.SATURDAY -> "Thứ Bảy"
            java.time.DayOfWeek.SUNDAY -> "Chủ Nhật"
        }
        append("$dayOfWeekName, ngày ${day.solarDate.day} tháng ${day.solarDate.month} năm ${day.solarDate.year}. ")
        if (day.lunarDate != null) {
            append("Âm lịch ngày ${day.lunarDate.day} tháng ${day.lunarDate.month}. ")
        }
        if (day.isCurrentMonth) {
            if (day.isHoangDao) {
                append("Ngày Hoàng Đạo. ")
            } else {
                append("Ngày Hắc Đạo. ")
            }
        }
        if (day.hasSpecialEvent) {
            append("Có sự kiện đặc biệt. ")
        }
    }

    val cellModifier = modifier
        .sizeIn(minWidth = 40.dp, minHeight = 48.dp)
        .aspectRatio(0.7f)
        .clip(RoundedCornerShape(12.dp))
        .background(containerColor)
        .then(
            if (day.isCurrentMonth) {
                Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Modifier
            }
        )
        .clickable(enabled = day.isCurrentMonth, onClick = onClick)
        .padding(horizontal = 2.dp, vertical = 4.dp)
        .then(if (!day.isCurrentMonth) Modifier.alpha(0.3f) else Modifier)
        .clearAndSetSemantics {
            contentDescription = accessibilityDescription
        }

    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ngày Dương
        Text(
            text = day.solarDate.day.toString(),
            color = solarDayColor,
            fontSize = 15.sp,
            fontWeight = solarDayFontWeight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Ngày Âm
        if (day.lunarDate != null) {
            val lunarText = if (day.lunarDate.day == 1) {
                "${day.lunarDate.day}/${day.lunarDate.month}"
            } else {
                day.lunarDate.day.toString()
            }
            Text(
                text = lunarText,
                color = lunarDayColor,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (day.isHoangDao && day.isCurrentMonth) FontWeight.Bold else FontWeight.Normal
            )
        } else {
            Spacer(modifier = Modifier.height(11.dp)) // Maintain space
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Chấm tròn nhỏ hiển thị sự kiện đặc biệt (luôn vẽ để giữ nguyên kích thước cell)
        Box(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(if (day.hasSpecialEvent) Color(0xFFF57C00) else Color.Transparent)
        )
    }
}
