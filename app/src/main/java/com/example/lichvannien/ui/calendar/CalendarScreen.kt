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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
import com.example.lichvannien.theme.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.time.YearMonth

private const val BASE_YEAR = 2000
private const val TOTAL_PAGES = 2400 // Covers years 2000 to 2200
private val DayCellShape = RoundedCornerShape(12.dp)
private val EventDotColor = Color(0xFFF57C00)

@Immutable
data class DayCellThemePalette(
    val primary: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val outlineVariant: Color,
    val hoangDaoBg: Color,
    val hoangDaoText: Color,
    val hacDaoBg: Color,
    val saturdayColor: Color,
    val sundayColor: Color,
    val isDark: Boolean
)

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

    // Sync ViewModel preload when page settles
    LaunchedEffect(pagerState.currentPage) {
        val settledMonth = yearMonthFromPage(pagerState.currentPage)
        viewModel.loadMonth(settledMonth.year, settledMonth.monthValue)
    }

    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val palette = remember(isDark, colorScheme) {
        DayCellThemePalette(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            onSurface = colorScheme.onSurface,
            outlineVariant = colorScheme.outlineVariant,
            hoangDaoBg = if (isDark) ColorHoangDaoBgDark else ColorHoangDaoBgLight,
            hoangDaoText = if (isDark) ColorHoangDaoTextDark else ColorHoangDaoTextLight,
            hacDaoBg = if (isDark) ColorHacDaoBgDark else ColorHacDaoBgLight,
            saturdayColor = if (isDark) ColorSaturdayDark else ColorSaturdayLight,
            sundayColor = if (isDark) ColorSundayDark else ColorSundayLight,
            isDark = isDark
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Isolated Header Section with derivedStateOf
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
            },
            onTodayClick = {
                val todayPage = pageFromYearMonth(YearMonth.now())
                coroutineScope.launch {
                    pagerState.animateScrollToPage(todayPage)
                }
            }
        )

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
                WeekdaysHeader(
                    isDark = isDark,
                    onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                    MonthPage(
                        page = page,
                        uiState = uiState,
                        viewModel = viewModel,
                        palette = palette,
                        onDayClick = onDayClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onTodayClick: () -> Unit,
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
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onPreviousClick) {
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

            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month"
                )
            }
        }

        FilledTonalButton(
            onClick = onTodayClick,
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
}

@Composable
fun WeekdaysHeader(
    isDark: Boolean,
    onSurfaceVariant: Color,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
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
}

@Composable
fun MonthPage(
    page: Int,
    uiState: CalendarUiState,
    viewModel: CalendarViewModel,
    palette: DayCellThemePalette,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pageMonth = remember(page) { yearMonthFromPage(page) }
    val days = uiState.monthDataMap[pageMonth] ?: viewModel.getMonthDays(pageMonth) ?: persistentListOf()

    CalendarGrid(
        days = days,
        palette = palette,
        onDayClick = onDayClick,
        modifier = modifier
    )
}

@Composable
fun CalendarGrid(
    days: ImmutableList<CalendarDayUiModel>,
    palette: DayCellThemePalette,
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
                        key(day.year, day.month, day.day) {
                            DayCell(
                                day = day,
                                palette = palette,
                                onDayClick = onDayClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
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
    day: CalendarDayUiModel,
    palette: DayCellThemePalette,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Background color (Today has highest priority, using primary color scheme)
    val containerColor = when {
        !day.isCurrentMonth -> Color.Transparent
        day.isToday -> palette.primary
        day.isHoangDao -> palette.hoangDaoBg
        else -> palette.hacDaoBg // Transparent
    }

    // Solar day text color (Today has onPrimary color)
    val solarDayColor = when {
        !day.isCurrentMonth -> palette.onSurface.copy(alpha = 0.3f)
        day.isToday -> palette.onPrimary
        day.isSaturday -> palette.saturdayColor
        day.isSunday -> palette.sundayColor
        day.isHoangDao -> palette.hoangDaoText
        else -> palette.onSurface
    }

    // Lunar day text color (Today has onPrimary with opacity)
    val lunarDayColor = when {
        !day.isCurrentMonth -> palette.onSurface.copy(alpha = 0.3f)
        day.isToday -> palette.onPrimary.copy(alpha = 0.8f)
        day.isHoangDao -> palette.hoangDaoText
        day.isSaturday -> palette.saturdayColor.copy(alpha = 0.8f)
        day.isSunday -> palette.sundayColor.copy(alpha = 0.8f)
        else -> palette.onSurface.copy(alpha = 0.6f)
    }

    val solarDayFontWeight = when {
        day.isToday || day.isSaturday || day.isSunday -> FontWeight.Bold
        else -> FontWeight.Medium
    }

    val density = LocalDensity.current
    val cornerRadiusPx = remember(density) { with(density) { 12.dp.toPx() } }
    val borderWidthPx = remember(density) { with(density) { 0.5.dp.toPx() } }
    val dotRadiusPx = remember(density) { with(density) { 1.5.dp.toPx() } }
    val dotBottomOffsetPx = remember(density) { with(density) { 4.dp.toPx() } }
    val borderColor = remember(palette.outlineVariant) { palette.outlineVariant.copy(alpha = 0.4f) }

    val cellModifier = modifier
        .fillMaxHeight()
        .clip(DayCellShape)
        .drawBehind {
            // Draw background
            if (containerColor != Color.Transparent) {
                drawRoundRect(
                    color = containerColor,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }
            // Draw border for current month days
            if (day.isCurrentMonth) {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = borderWidthPx)
                )
            }
            // Draw special event dot at bottom center
            if (day.hasSpecialEvent) {
                drawCircle(
                    color = EventDotColor,
                    radius = dotRadiusPx,
                    center = Offset(size.width / 2f, size.height - dotBottomOffsetPx)
                )
            }
        }
        .clickable(
            enabled = day.isCurrentMonth,
            onClick = { onDayClick(day.year, day.month, day.day) }
        )
        .padding(horizontal = 2.dp, vertical = 2.dp)
        .then(if (!day.isCurrentMonth) Modifier.alpha(0.3f) else Modifier)
        .clearAndSetSemantics {
            contentDescription = day.contentDescription
        }

    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ngày Dương
        Text(
            text = day.solarDayText,
            color = solarDayColor,
            fontSize = 15.sp,
            fontWeight = solarDayFontWeight,
            textAlign = TextAlign.Center
        )

        // Ngày Âm
        if (day.lunarDayText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = day.lunarDayText,
                color = lunarDayColor,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (day.isHoangDao && day.isCurrentMonth) FontWeight.Bold else FontWeight.Normal
            )
        } else {
            Spacer(modifier = Modifier.height(13.dp)) // Maintain alignment
        }
    }
}
