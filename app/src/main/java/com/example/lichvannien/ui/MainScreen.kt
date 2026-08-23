package com.example.lichvannien.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.lichvannien.R
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.util.EasternFengShuiHelper
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.theme.AppNavActiveBg
import com.example.lichvannien.ui.ai.AiChatScreen
import com.example.lichvannien.ui.calendar.CalendarScreen
import com.example.lichvannien.ui.navigation.Screen
import com.example.lichvannien.ui.search.SearchDialog
import com.example.lichvannien.ui.task.TaskScreen
import com.example.lichvannien.ui.today.TodayScreen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences? = null
) {
    var currentTab by remember { mutableStateOf(Screen.Today.route) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showChangeBirthdayDialog by remember { mutableStateOf(false) }
    var showFengShuiInfoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val birthdayTriple by (userPreferences?.birthdayFlow ?: flowOf(Triple(0, 0, 0)))
        .collectAsStateWithLifecycle(initialValue = Triple(0, 0, 0))

    val birthYear = if (birthdayTriple.third > 1900) birthdayTriple.third else 1995
    val zodiacInfo = remember(birthYear) {
        EasternFengShuiHelper.getZodiacInfo(birthYear)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppHeaderBlue)
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_header_title),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "Phong Thủy & Lịch Âm Dương",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Thẻ Bản Mệnh Phương Đông
                            Surface(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch { drawerState.close() }
                                        showFengShuiInfoDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = zodiacInfo.animalEmoji,
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Tuổi ${zodiacInfo.canChiYear} (${zodiacInfo.year})",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mệnh: ${zodiacInfo.napAm}",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu Items
                    NavigationDrawerItem(
                        label = { Text("Hôm nay", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        selected = currentTab == Screen.Today.route,
                        onClick = {
                            currentTab = Screen.Today.route
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Lịch tháng", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        selected = currentTab == Screen.CalendarMonth.route,
                        onClick = {
                            currentTab = Screen.CalendarMonth.route
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Lịch trình & Việc cần làm", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
                        selected = currentTab == Screen.Task.route,
                        onClick = {
                            currentTab = Screen.Task.route
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Xem Bản Mệnh & Phong Thủy", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFE65100)) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showFengShuiInfoDialog = true
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Trợ lý AI Phong Thủy", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        selected = currentTab == Screen.AiChat.route,
                        onClick = {
                            currentTab = Screen.AiChat.route
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    NavigationDrawerItem(
                        label = { Text("Đổi ngày sinh & Bản mệnh", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showChangeBirthdayDialog = true
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Thông tin ứng dụng", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showAboutDialog = true
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.app_header_title),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppHeaderBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(64.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            NavTabItem(
                                route = Screen.Today.route,
                                label = stringResource(R.string.tab_today_label),
                                icon = Icons.Default.CalendarToday
                            ),
                            NavTabItem(
                                route = Screen.CalendarMonth.route,
                                label = stringResource(R.string.tab_calendar_label),
                                icon = Icons.Default.CalendarMonth
                            ),
                            NavTabItem(
                                route = Screen.Task.route,
                                label = stringResource(R.string.tab_task_label),
                                icon = Icons.Default.CheckBox
                            ),
                            NavTabItem(
                                route = Screen.AiChat.route,
                                label = stringResource(R.string.tab_ai_label),
                                icon = Icons.Default.Psychology
                            )
                        )

                        tabs.forEach { item ->
                            val isSelected = currentTab == item.route
                            CustomBottomNavItem(
                                item = item,
                                isSelected = isSelected,
                                onClick = { currentTab = item.route },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = currentTab,
                    label = "MainTabCrossfade"
                ) { tab ->
                    when (tab) {
                        Screen.Today.route -> {
                            TodayScreen(
                                onDayDetailClick = { year, month, day ->
                                    navController.navigate(Screen.DayDetail.createRoute(year, month, day))
                                }
                            )
                        }
                        Screen.CalendarMonth.route, Screen.Calendar.route -> {
                            CalendarScreen(
                                onDayClick = { year, month, day ->
                                    navController.navigate(Screen.DayDetail.createRoute(year, month, day))
                                }
                            )
                        }
                        Screen.Task.route -> {
                            TaskScreen()
                        }
                        Screen.AiChat.route -> {
                            AiChatScreen()
                        }
                    }
                }
            }
        }
    }

    // Search Dialog
    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onNavigateToDay = { year, month, day ->
                navController.navigate(Screen.DayDetail.createRoute(year, month, day))
            }
        )
    }

    // Feng Shui Info Dialog (Tử vi Bản Mệnh Phương Đông)
    if (showFengShuiInfoDialog) {
        AlertDialog(
            onDismissRequest = { showFengShuiInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = zodiacInfo.animalEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Tuổi ${zodiacInfo.canChiYear}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Năm sinh: ${zodiacInfo.year}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Bản Mệnh Ngũ Hành:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(text = "Mệnh ${zodiacInfo.nguHanh.nameVi} • ${zodiacInfo.napAm}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Text(text = "🎨 Màu sắc hợp mệnh:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = zodiacInfo.luckyColors.joinToString(", "), fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(text = "✨ Quy luật tương sinh:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = zodiacInfo.suitableElements.joinToString("\n"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(text = "🧭 Lời khuyên:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = zodiacInfo.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFengShuiInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue)
                ) {
                    Text("Đã hiểu")
                }
            }
        )
    }

    // Change Birthday Dialog
    if (showChangeBirthdayDialog && userPreferences != null) {
        ChangeBirthdayDialog(
            currentDay = if (birthdayTriple.first != 0) birthdayTriple.first else LocalDate.now().dayOfMonth,
            currentMonth = if (birthdayTriple.second != 0) birthdayTriple.second else LocalDate.now().monthValue,
            currentYear = if (birthdayTriple.third != 0) birthdayTriple.third else 1995,
            onDismiss = { showChangeBirthdayDialog = false },
            onSave = { day, month, year ->
                coroutineScope.launch {
                    userPreferences.saveBirthday(day, month, year)
                }
                showChangeBirthdayDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AppHeaderBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lịch Việt 2026", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Phiên bản: 1.0.0 (Thuần túy Phương Đông)")
                    Text("Ứng dụng Lịch Vạn Niên & Lịch Việt hiện đại tích hợp AI, tra cứu ngày giờ Hoàng Đạo/Hắc Đạo, Bản Mệnh Ngũ Hành Phương Đông và quản lý lịch trình công việc tiện lợi.")
                    Text("© 2026 Lịch Việt. Vạn sự như ý.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeBirthdayDialog(
    currentDay: Int,
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onSave: (day: Int, month: Int, year: Int) -> Unit
) {
    var dayStr by remember { mutableStateOf(currentDay.toString()) }
    var monthStr by remember { mutableStateOf(currentMonth.toString()) }
    var yearStr by remember { mutableStateOf(currentYear.toString()) }

    var showDatePicker by remember { mutableStateOf(false) }

    val parsedYear = yearStr.toIntOrNull() ?: currentYear
    val calculatedZodiac = remember(parsedYear) {
        EasternFengShuiHelper.getZodiacInfo(parsedYear)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        dayStr = localDate.dayOfMonth.toString()
                        monthStr = localDate.monthValue.toString()
                        yearStr = localDate.year.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi ngày sinh & Bản mệnh", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppHeaderBlue.copy(alpha = 0.12f),
                        contentColor = AppHeaderBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chọn nhanh từ Lịch", fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "Hoặc chỉnh sửa trực tiếp ngày / tháng / năm:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                dayStr = input
                            }
                        },
                        label = { Text("Ngày") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                monthStr = input
                            }
                        },
                        label = { Text("Tháng") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                yearStr = input
                            }
                        },
                        label = { Text("Năm") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = calculatedZodiac.animalEmoji,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = "Tuổi ${calculatedZodiac.canChiYear} ($parsedYear)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Mệnh: ${calculatedZodiac.napAm}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDay = dayStr.toIntOrNull()?.coerceIn(1, 31) ?: currentDay
                    val finalMonth = monthStr.toIntOrNull()?.coerceIn(1, 12) ?: currentMonth
                    val finalYear = yearStr.toIntOrNull()?.coerceIn(1900, 2100) ?: currentYear
                    onSave(finalDay, finalMonth, finalYear)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppHeaderBlue)
            ) {
                Text("Lưu lại")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun CustomBottomNavItem(
    item: NavTabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBgColor = AppNavActiveBg
    val activeContentColor = Color.White
    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(activeBgColor)
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = activeContentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeContentColor
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = inactiveContentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = inactiveContentColor
                )
            }
        }
    }
}
