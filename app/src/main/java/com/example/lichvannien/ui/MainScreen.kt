package com.example.lichvannien.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.lichvannien.R
import com.example.lichvannien.ui.calendar.CalendarScreen
import com.example.lichvannien.ui.horoscope.HoroscopeScreen
import com.example.lichvannien.ui.navigation.Screen

@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(Screen.Calendar.route) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == Screen.Calendar.route,
                    onClick = { currentTab = Screen.Calendar.route },
                    icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Lịch") },
                    label = { Text(text = stringResource(R.string.tab_calendar)) }
                )
                NavigationBarItem(
                    selected = currentTab == Screen.Horoscope.route,
                    onClick = { currentTab = Screen.Horoscope.route },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Tử vi") },
                    label = { Text(text = stringResource(R.string.tab_horoscope)) }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                when (tab) {
                    Screen.Calendar.route -> {
                        CalendarScreen(
                            onDayClick = { year, month, day ->
                                navController.navigate(Screen.DayDetail.createRoute(year, month, day))
                            }
                        )
                    }
                    Screen.Horoscope.route -> {
                        HoroscopeScreen()
                    }
                }
            }
        }
    }
}
