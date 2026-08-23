package com.example.lichvannien.ui.horoscope

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.domain.model.Horoscope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    modifier: Modifier = Modifier,
    viewModel: HoroscopeViewModel = hiltViewModel()
) {
    val zodiacSignState by viewModel.zodiacSign.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val horoscopeState by viewModel.horoscope.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorState by viewModel.error.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.refresh() },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header hiển thị cung hoàng đạo của người dùng
            val sign = zodiacSignState
            if (sign != null) {
                item(key = "zodiac_sign_header") {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sign.emoji,
                                fontSize = 48.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.horoscope_welcome_format, sign.nameVi, sign.emoji),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(
                                        R.string.zodiac_range_format,
                                        "",
                                        sign.startDay,
                                        sign.startMonth,
                                        sign.endDay,
                                        sign.endMonth
                                    ).trim(),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. TabRow lựa chọn khoảng thời gian
            item(key = "tab_row") {
                val tabs = listOf(
                    HoroscopeTab.TODAY to R.string.tab_today,
                    HoroscopeTab.WEEK to R.string.tab_week,
                    HoroscopeTab.MONTH to R.string.tab_month
                )
                TabRow(
                    selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEach { (tab, labelRes) ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = { Text(text = stringResource(labelRes), fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // 3. Nội dung chính: Shimmer / Lỗi / Dữ liệu thực tế
            val error = errorState
            val horoscope = horoscopeState

            if (isLoading) {
                // Skeleton Shimmer Loading
                items(4, key = { "shimmer_$it" }) {
                    ShimmerCardItem()
                }
            } else if (error != null) {
                // Lỗi tải dữ liệu
                item(key = "error_view") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error_horoscope_load),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text(text = stringResource(R.string.btn_retry))
                        }
                    }
                }
            } else if (horoscope != null) {
                // Thẻ Tổng quan
                item(key = "overview_card") {
                    HoroscopeCardWrapper(index = 0, horoscopeKey = horoscope.description) {
                        HoroscopeDetailCard(
                            title = stringResource(R.string.card_overview),
                            content = horoscope.description,
                            primaryColor = true
                        )
                    }
                }

                // Các Thẻ Chỉ số Cát Hung
                item(key = "compatibility_mood_card") {
                    HoroscopeCardWrapper(index = 1, horoscopeKey = horoscope.description) {
                        CardRow(
                            title1 = stringResource(R.string.card_compatibility),
                            content1 = horoscope.compatibility,
                            title2 = stringResource(R.string.card_mood),
                            content2 = horoscope.mood
                        )
                    }
                }

                item(key = "color_lucky_number_card") {
                    HoroscopeCardWrapper(index = 2, horoscopeKey = horoscope.description) {
                        CardRow(
                            title1 = stringResource(R.string.card_color),
                            content1 = horoscope.color,
                            title2 = stringResource(R.string.card_lucky_number),
                            content2 = horoscope.luckyNumber
                        )
                    }
                }

                item(key = "lucky_time_card") {
                    HoroscopeCardWrapper(index = 3, horoscopeKey = horoscope.description) {
                        HoroscopeDetailCard(
                            title = stringResource(R.string.card_lucky_time),
                            content = horoscope.luckyTime
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoroscopeCardWrapper(
    index: Int,
    horoscopeKey: String,
    content: @Composable () -> Unit
) {
    key(horoscopeKey) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            visible = true
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500, delayMillis = index * 100)) +
                    slideInVertically(animationSpec = tween(500, delayMillis = index * 100)) { it / 4 }
        ) {
            content()
        }
    }
}

@Composable
fun HoroscopeDetailCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    primaryColor: Boolean = false
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (primaryColor) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (primaryColor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = if (primaryColor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CardRow(
    title1: String,
    content1: String,
    title2: String,
    content2: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title1,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title2,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content2,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ShimmerCardItem(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(16.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shimmerEffect()
            )
        }
    }
}

// Fade effect shimmer helper conforming to standard M3 theme
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    this.background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
}
