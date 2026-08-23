package com.example.lichvannien.ui.detail

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.lichvannien.R
import com.example.lichvannien.domain.model.DayDetail
import com.example.lichvannien.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    year: Int,
    month: Int,
    day: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayDetailViewModel = hiltViewModel()
) {
    val dayDetailState by viewModel.dayDetail.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_day_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val detail = dayDetailState
            if (detail == null) {
                // Loading Indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Content with Fade-In Animation when loaded
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: Thông tin ngày
                        item(key = "info_card") {
                            InfoCard(detail = detail)
                        }

                        // Card 2: Ngày tốt / xấu
                        item(key = "auspicious_card") {
                            AuspiciousCard(detail = detail)
                        }

                        // Card 3: Giờ hoàng đạo
                        item(key = "hours_card") {
                            HoursCard(detail = detail)
                        }

                        // Card 4: Sự kiện trong ngày
                        item(key = "events_card") {
                            EventsCard(detail = detail)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(detail: DayDetail, modifier: Modifier = Modifier) {
    // Tính toán thứ bằng tiếng Việt
    val localDate = LocalDate.of(detail.solarDate.year, detail.solarDate.month, detail.solarDate.day)
    val dayOfWeekRes = when (localDate.dayOfWeek.value) {
        1 -> R.string.day_monday
        2 -> R.string.day_tuesday
        3 -> R.string.day_wednesday
        4 -> R.string.day_thursday
        5 -> R.string.day_friday
        6 -> R.string.day_saturday
        else -> R.string.day_sunday
    }

    val solarDateStr = "${stringResource(dayOfWeekRes)}, ${detail.solarDate.day}/${detail.solarDate.month}/${detail.solarDate.year}"
    val leapSuffix = if (detail.lunarDate.isLeapMonth) stringResource(R.string.lunar_leap_suffix) else ""
    val lunarDateStr = stringResource(
        R.string.lunar_date_format,
        detail.lunarDate.day,
        detail.lunarDate.month,
        detail.lunarDate.year
    ) + leapSuffix

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.card_info_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Ngày dương
            Column {
                Text(
                    text = "Dương Lịch",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = solarDateStr,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Ngày âm
            Column {
                Text(
                    text = "Âm Lịch",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = lunarDateStr,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Can Chi
            Column {
                Text(
                    text = "Can Chi",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(R.string.can_chi_day, detail.lunarDate.canChiDay), fontSize = 14.sp)
                Text(text = stringResource(R.string.can_chi_month, detail.lunarDate.canChiMonth), fontSize = 14.sp)
                Text(text = stringResource(R.string.can_chi_year, detail.lunarDate.canChiYear), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AuspiciousCard(detail: DayDetail, modifier: Modifier = Modifier) {
    val isHoangDao = detail.auspicious.isHoangDao
    val badgeText = if (isHoangDao) stringResource(R.string.label_hoang_dao) else stringResource(R.string.label_hac_dao)
    val isDark = isSystemInDarkTheme()
    val badgeColor = if (isHoangDao) {
        if (isDark) ColorHoangDaoBgDark else ColorHoangDaoBgLight
    } else {
        if (isDark) ColorHacDaoBgDark else ColorHacDaoBgLight
    }
    val badgeTextColor = if (isHoangDao) {
        if (isDark) ColorHoangDaoTextDark else ColorHoangDaoTextLight
    } else {
        if (isDark) ColorHacDaoTextDark else ColorHacDaoTextLight
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.card_auspicious_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Trực
                Text(
                    text = stringResource(R.string.label_truc, detail.auspicious.truc),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Badge Hoàng đạo / Hắc đạo
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(LayoutHorizontalApi::class)
@Composable
fun HoursCard(detail: DayDetail, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.card_hours_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Hiển thị dạng FlowRow xếp chip giờ
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                detail.auspicious.gioHoangDao.forEach { hour ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(text = hour, fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventsCard(detail: DayDetail, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.card_events_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (detail.specialDays.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_events),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.specialDays.forEach { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = event.icon ?: "📅",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = event.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Layout annotation
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class LayoutHorizontalApi
