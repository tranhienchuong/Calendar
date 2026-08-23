package com.example.lichvannien.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.ui.calendar.CalendarScreen
import com.example.lichvannien.ui.calendar.CalendarViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val specialDayRepository = mockk<SpecialDayRepository>(relaxed = true)

    @Test
    fun testCalendarNavigation() {
        coEvery { specialDayRepository.getEventsForSolarMonth(any(), any()) } returns emptyList()
        coEvery { specialDayRepository.getEventsForLunarMonth(any(), any()) } returns emptyList()

        val viewModel = CalendarViewModel(specialDayRepository)

        composeTestRule.setContent {
            CalendarScreen(
                onDayClick = { _, _, _ -> },
                viewModel = viewModel
            )
        }

        // Nhấn nút sang tháng tiếp theo
        composeTestRule.onNodeWithContentDescription("Tháng sau").performClick()
        
        // Nhấn nút quay về tháng trước
        composeTestRule.onNodeWithContentDescription("Tháng trước").performClick()
    }
}
