package com.example.lichvannien.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Calendar : Screen("calendar")
    object Today : Screen("today")
    object CalendarMonth : Screen("calendar_month")
    object Task : Screen("task")
    object AiChat : Screen("ai_chat")
    object DayDetail : Screen("day_detail/{year}/{month}/{day}") {
        fun createRoute(year: Int, month: Int, day: Int) = "day_detail/$year/$month/$day"
    }
}
