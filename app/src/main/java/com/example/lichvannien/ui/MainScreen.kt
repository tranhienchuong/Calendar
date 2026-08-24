package com.example.lichvannien.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.lichvannien.R
import com.example.lichvannien.domain.util.EasternFengShuiHelper
import com.example.lichvannien.theme.AppHeaderBlue
import com.example.lichvannien.theme.AppNavActiveBg
import com.example.lichvannien.ui.ai.AiChatScreen
import com.example.lichvannien.ui.calendar.CalendarScreen
import com.example.lichvannien.ui.navigation.Screen
import com.example.lichvannien.ui.search.SearchDialog
import com.example.lichvannien.ui.task.TaskScreen
import com.example.lichvannien.ui.today.TodayScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    var currentTab by rememberSaveable { mutableStateOf(Screen.Today.route) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showChangeBirthdayDialog by remember { mutableStateOf(false) }
    var showFengShuiInfoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val birthday by mainViewModel.birthdayState.collectAsStateWithLifecycle()

    val birthYear = if ((birthday?.year ?: 0) > 1900) birthday!!.year else 1995
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
                            .background(Color(0xFF1E1E1E))
                            .padding(horizontal = 20.dp, vertical = 26.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFBBF24)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF1E1E1E),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_header_title),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        text = "Phong Thủy & Lịch Âm Dương",
                                        color = Color(0xFFFBBF24).copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Thẻ Bản Mệnh Phương Đông
                            Surface(
                                color = Color(0xFF2C2C2C),
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
                                            color = Color(0xFFFBBF24).copy(alpha = 0.9f),
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
                if (currentTab != Screen.Task.route) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isSystemInDarkTheme()) Color(0xFF2C2417) else Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.app_header_title),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSearchDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Tìm kiếm",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            bottomBar = {
                val isDark = isSystemInDarkTheme()
                val activeContentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                val activePillBg = if (isDark) Color(0xFF2C2417) else Color(0xFFFEF3C7)
                val inactiveContentColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

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
                        tabs.forEach { item ->
                            val isSelected = currentTab == item.route
                            CustomBottomNavItem(
                                item = item,
                                isSelected = isSelected,
                                activeContentColor = activeContentColor,
                                activePillBg = activePillBg,
                                inactiveContentColor = inactiveContentColor,
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
                val visitedTabs = remember { mutableStateListOf(currentTab) }
                LaunchedEffect(currentTab) {
                    if (!visitedTabs.contains(currentTab)) {
                        visitedTabs.add(currentTab)
                    }
                }

                val onTodayDayDetailClick = remember(navController) {
                    { year: Int, month: Int, day: Int ->
                        navController.navigate(Screen.DayDetail.createRoute(year, month, day))
                    }
                }
                val onCalendarDayClick = remember(navController) {
                    { year: Int, month: Int, day: Int ->
                        currentTab = Screen.CalendarMonth.route
                        navController.navigate(Screen.DayDetail.createRoute(year, month, day))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (visitedTabs.contains(Screen.Today.route)) {
                        val isTodayActive = currentTab == Screen.Today.route
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .tabKeepAlive(isTodayActive)
                        ) {
                            TodayScreen(onDayDetailClick = onTodayDayDetailClick)
                        }
                    }

                    val isCalendarVisited = visitedTabs.contains(Screen.CalendarMonth.route) || visitedTabs.contains(Screen.Calendar.route)
                    if (isCalendarVisited) {
                        val isCalendarActive = currentTab == Screen.CalendarMonth.route || currentTab == Screen.Calendar.route
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .tabKeepAlive(isCalendarActive)
                        ) {
                            CalendarScreen(onDayClick = onCalendarDayClick)
                        }
                    }

                    if (visitedTabs.contains(Screen.Task.route)) {
                        val isTaskActive = currentTab == Screen.Task.route
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .tabKeepAlive(isTaskActive)
                        ) {
                            TaskScreen()
                        }
                    }

                    if (visitedTabs.contains(Screen.AiChat.route)) {
                        val isAiActive = currentTab == Screen.AiChat.route
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .tabKeepAlive(isAiActive)
                        ) {
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
                currentTab = Screen.CalendarMonth.route
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
    if (showChangeBirthdayDialog) {
        ChangeBirthdayDialog(
            currentDay = if ((birthday?.day ?: 0) in 1..31) birthday!!.day else LocalDate.now().dayOfMonth,
            currentMonth = if ((birthday?.month ?: 0) in 1..12) birthday!!.month else LocalDate.now().monthValue,
            currentYear = if ((birthday?.year ?: 0) > 1900) birthday!!.year else 1995,
            onDismiss = { showChangeBirthdayDialog = false },
            onSave = { day, month, year ->
                mainViewModel.saveBirthday(day, month, year)
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
    activeContentColor: Color,
    activePillBg: Color,
    inactiveContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Surface(
                color = activePillBg,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = activeContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeContentColor
                    )
                }
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = inactiveContentColor
                )
            }
        }
    }
}

private fun Modifier.tabKeepAlive(isActive: Boolean): Modifier {
    return this
        .alpha(if (isActive) 1f else 0f)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                if (isActive) {
                    placeable.place(0, 0)
                }
            }
        }
        .then(if (!isActive) Modifier.clearAndSetSemantics { } else Modifier)
}
