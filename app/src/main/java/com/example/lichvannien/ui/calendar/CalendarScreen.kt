package com.example.lichvannien.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.example.lichvannien.domain.model.CalendarDay
import com.example.lichvannien.theme.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
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

    val initialPage = remember {
        val now = YearMonth.now()
        pageFromYearMonth(now)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { TOTAL_PAGES })

    // Active month for header display: updates immediately during drag/settling
    val activeMonth = remember(pagerState.currentPage, pagerState.targetPage, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            yearMonthFromPage(pagerState.targetPage)
        } else {
            yearMonthFromPage(pagerState.currentPage)
        }
    }

    // Sync ViewModel preload when page settles
    LaunchedEffect(pagerState.currentPage) {
        val settledMonth = yearMonthFromPage(pagerState.currentPage)
        viewModel.loadMonth(settledMonth.year, settledMonth.monthValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }

                Text(
                    text = stringResource(
                        R.string.calendar_header_format,
                        activeMonth.monthValue,
                        activeMonth.year
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = {
                    if (pagerState.currentPage < TOTAL_PAGES - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            FilledTonalButton(
                onClick = {
                    val todayPage = pageFromYearMonth(YearMonth.now())
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(todayPage)
                    }
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

        // Calendar Card containing Weekday Header & HorizontalPager for Months
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                // Weekdays Header Row
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

                // High performance HorizontalPager for smooth 1:1 touch tracking and snapping
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val pageMonth = remember(page) { yearMonthFromPage(page) }
                    val days = uiState.monthDataMap[pageMonth] ?: viewModel.getMonthDays(pageMonth) ?: persistentListOf()

                    CalendarGrid(
                        days = days,
                        onDayClick = onDayClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    days: ImmutableList<CalendarDay>,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (rowIndex in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (colIndex in 0 until 7) {
                    val dayIndex = rowIndex * 7 + colIndex
                    if (dayIndex < days.size) {
                        val day = days[dayIndex]
                        DayCell(
                            day = day,
                            onClick = {
                                onDayClick(day.solarDate.year, day.solarDate.month, day.solarDate.day)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
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
        .fillMaxHeight()
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
        .padding(horizontal = 2.dp, vertical = 2.dp)
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
