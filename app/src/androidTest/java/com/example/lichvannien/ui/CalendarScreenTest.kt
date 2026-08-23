package com.example.lichvannien.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.ui.calendar.CalendarScreen
import com.example.lichvannien.ui.calendar.CalendarViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val specialDayRepository = mockk<SpecialDayRepository>(relaxed = true)

    @Test
    fun testCalendarNavigation_buttonsAndDayClick() {
        coEvery { specialDayRepository.getEventsForSolarMonth(any()) } returns emptyList()
        coEvery { specialDayRepository.getEventsForLunarMonth(any()) } returns emptyList()

        var clickedDay: Triple<Int, Int, Int>? = null

        val viewModel = CalendarViewModel(
            com.example.lichvannien.domain.util.LunarConverter,
            com.example.lichvannien.domain.util.AuspiciousCalculator,
            specialDayRepository
        )

        composeTestRule.setContent {
            CalendarScreen(
                onDayClick = { y, m, d -> clickedDay = Triple(y, m, d) },
                viewModel = viewModel
            )
        }

        // Nhấn nút sang tháng tiếp theo
        composeTestRule.onNodeWithContentDescription("Next Month").performClick()
        composeTestRule.waitForIdle()

        // Nhấn nút quay về tháng trước
        composeTestRule.onNodeWithContentDescription("Previous Month").performClick()
        composeTestRule.waitForIdle()

        // Nhấn nút Hôm nay
        composeTestRule.onNodeWithText("Hôm nay").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun testCalendarSwipeGestures() {
        coEvery { specialDayRepository.getEventsForSolarMonth(any()) } returns emptyList()
        coEvery { specialDayRepository.getEventsForLunarMonth(any()) } returns emptyList()

        val viewModel = CalendarViewModel(
            com.example.lichvannien.domain.util.LunarConverter,
            com.example.lichvannien.domain.util.AuspiciousCalculator,
            specialDayRepository
        )

        composeTestRule.setContent {
            CalendarScreen(
                onDayClick = { _, _, _ -> },
                viewModel = viewModel
            )
        }

        // Swipe left to go to next month
        composeTestRule.onNodeWithContentDescription("Next Month").performClick()
        composeTestRule.waitForIdle()

        // Swipe right to go to previous month
        composeTestRule.onNodeWithContentDescription("Previous Month").performClick()
        composeTestRule.waitForIdle()
    }
}
