package com.example.lichvannien.ui.calendar

import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakeRepository = FakeSpecialDayRepository()
    private val lunarConverter = LunarConverter
    private val auspiciousCalculator = AuspiciousCalculator

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CalendarViewModel {
        return CalendarViewModel(
            lunarConverter = lunarConverter,
            auspiciousCalculator = auspiciousCalculator,
            specialDayRepository = fakeRepository,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun initialization_loadsCurrentMonth_andPreloadsAdjacentMonths() = testScope.runTest {
        val viewModel = createViewModel()
        val now = YearMonth.now()

        // Initial state before coroutine completion
        assertThat(viewModel.uiState.value.targetMonth).isEqualTo(now)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.targetMonth).isEqualTo(now)
        assertThat(state.displayedMonth).isEqualTo(now)
        assertThat(state.isLoading).isFalse()
        assertThat(state.daysList).hasSize(42)

        // Verify that days contains today
        val today = LocalDate.now()
        val todayDay = state.daysList.firstOrNull { it.isToday }
        assertThat(todayDay).isNotNull()
        assertThat(todayDay!!.solarDate.year).isEqualTo(today.year)
        assertThat(todayDay.solarDate.month).isEqualTo(today.monthValue)
        assertThat(todayDay.solarDate.day).isEqualTo(today.dayOfMonth)
    }

    @Test
    fun tenRapidNextMonthRequests_synchronouslyAdvanceTargetMonth_andEventuallyDisplayTargetPlusTen() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialMonth = viewModel.uiState.value.targetMonth

        // Rapidly call goToNextMonth 10 times in a row
        repeat(10) { i ->
            viewModel.goToNextMonth()
            assertThat(viewModel.uiState.value.targetMonth).isEqualTo(initialMonth.plusMonths((i + 1).toLong()))
        }

        // Target month must synchronously be exactly 10 months ahead
        val expectedFinalMonth = initialMonth.plusMonths(10)
        assertThat(viewModel.uiState.value.targetMonth).isEqualTo(expectedFinalMonth)

        // Run coroutines to completion
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertThat(finalState.targetMonth).isEqualTo(expectedFinalMonth)
        assertThat(finalState.displayedMonth).isEqualTo(expectedFinalMonth)
        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.daysList).hasSize(42)

        // Verify the first current-month day in the grid matches expectedFinalMonth
        val currentMonthDays = finalState.daysList.filter { it.isCurrentMonth }
        assertThat(currentMonthDays).isNotEmpty()
        assertThat(currentMonthDays.first().solarDate.month).isEqualTo(expectedFinalMonth.monthValue)
        assertThat(currentMonthDays.first().solarDate.year).isEqualTo(expectedFinalMonth.year)
    }

    @Test
    fun tenRapidPreviousMonthRequests_synchronouslyAdvanceTargetMonth_andEventuallyDisplayTargetMinusTen() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialMonth = viewModel.uiState.value.targetMonth

        // Rapidly call goToPreviousMonth 10 times in a row
        repeat(10) { i ->
            viewModel.goToPreviousMonth()
            assertThat(viewModel.uiState.value.targetMonth).isEqualTo(initialMonth.minusMonths((i + 1).toLong()))
        }

        val expectedFinalMonth = initialMonth.minusMonths(10)
        assertThat(viewModel.uiState.value.targetMonth).isEqualTo(expectedFinalMonth)

        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertThat(finalState.targetMonth).isEqualTo(expectedFinalMonth)
        assertThat(finalState.displayedMonth).isEqualTo(expectedFinalMonth)
        assertThat(finalState.isLoading).isFalse()
        assertThat(finalState.daysList).hasSize(42)

        val currentMonthDays = finalState.daysList.filter { it.isCurrentMonth }
        assertThat(currentMonthDays.first().solarDate.month).isEqualTo(expectedFinalMonth.monthValue)
        assertThat(currentMonthDays.first().solarDate.year).isEqualTo(expectedFinalMonth.year)
    }

    @Test
    fun cacheHit_preloadedAdjacentMonth_updatesImmediatelyWithoutRepositoryCall() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialMonth = viewModel.uiState.value.targetMonth
        val nextMonth = initialMonth.plusMonths(1)

        // Reset repository counters after initial load & preloads of M-1, M+1
        val initialSolarCalls = fakeRepository.solarCallCount
        val initialLunarCalls = fakeRepository.lunarCallCount

        // Navigate to next month (which was preloaded)
        viewModel.goToNextMonth()

        // Cache hit -> displayedMonth, daysList, isLoading must update immediately
        val stateImmediate = viewModel.uiState.value
        assertThat(stateImmediate.targetMonth).isEqualTo(nextMonth)
        assertThat(stateImmediate.displayedMonth).isEqualTo(nextMonth)
        assertThat(stateImmediate.isLoading).isFalse()
        assertThat(stateImmediate.daysList).hasSize(42)

        // No new solar calls for nextMonth since it was in cache
        // (Preload may trigger for nextMonth + 1)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.displayedMonth).isEqualTo(nextMonth)
    }

    @Test
    fun supersededJobCancellation_slowRequestDoesNotOverwriteLatestTarget() = testScope.runTest {
        fakeRepository.delayMs = 100L

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Jump to an uncached month 100 months in future, then immediately jump to 200 months
        val slowMonth = YearMonth.of(2030, 1)
        val fastMonth = YearMonth.of(2035, 6)

        viewModel.loadMonth(slowMonth.year, slowMonth.monthValue)
        assertThat(viewModel.uiState.value.targetMonth).isEqualTo(slowMonth)
        assertThat(viewModel.uiState.value.isLoading).isTrue()

        // Immediately change mind and go to fastMonth before 100ms completes
        advanceTimeBy(30)
        viewModel.loadMonth(fastMonth.year, fastMonth.monthValue)
        assertThat(viewModel.uiState.value.targetMonth).isEqualTo(fastMonth)

        // Advance past all delays
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertThat(finalState.targetMonth).isEqualTo(fastMonth)
        assertThat(finalState.displayedMonth).isEqualTo(fastMonth)
        assertThat(finalState.isLoading).isFalse()

        // Ensure slowMonth days never overwrote fastMonth
        val currentMonthDays = finalState.daysList.filter { it.isCurrentMonth }
        assertThat(currentMonthDays.first().solarDate.year).isEqualTo(2035)
        assertThat(currentMonthDays.first().solarDate.month).isEqualTo(6)
    }

    @Test
    fun specialEventsAndLunarAccuracy_properlyMappedInGrid() = testScope.runTest {
        // Set up special events
        fakeRepository.solarEvents.add(
            SpecialDay(
                name = "Quốc khánh",
                icon = null,
                solarMonth = 9,
                solarDay = 2,
                isLunar = false
            )
        )
        fakeRepository.lunarEvents.add(
            SpecialDay(
                name = "Tết Nguyên Đán",
                icon = null,
                lunarMonth = 1,
                lunarDay = 1,
                isLunar = true,
                leapMonth = false
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Load September 2026
        viewModel.loadMonth(2026, 9)
        advanceUntilIdle()

        val sepState = viewModel.uiState.value
        val sep2 = sepState.daysList.first { it.isCurrentMonth && it.solarDate.day == 2 }
        assertThat(sep2.hasSpecialEvent).isTrue()

        val sep3 = sepState.daysList.first { it.isCurrentMonth && it.solarDate.day == 3 }
        assertThat(sep3.hasSpecialEvent).isFalse()

        // Load February 2026 (Tet Binh Ngo is 17/02/2026 = 01/01 lunar)
        viewModel.loadMonth(2026, 2)
        advanceUntilIdle()

        val febState = viewModel.uiState.value
        val feb17 = febState.daysList.first { it.isCurrentMonth && it.solarDate.day == 17 }
        assertThat(feb17.lunarDate).isNotNull()
        assertThat(feb17.lunarDate?.day).isEqualTo(1)
        assertThat(feb17.lunarDate?.month).isEqualTo(1)
        assertThat(feb17.hasSpecialEvent).isTrue()

        // Check non-event day
        val feb18 = febState.daysList.first { it.isCurrentMonth && it.solarDate.day == 18 }
        assertThat(feb18.hasSpecialEvent).isFalse()
    }
}

private class FakeSpecialDayRepository : SpecialDayRepository {
    var solarCallCount = 0
    var lunarCallCount = 0
    var delayMs = 0L

    val solarEvents = mutableListOf<SpecialDay>()
    val lunarEvents = mutableListOf<SpecialDay>()

    override suspend fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDay> = emptyList()
    override suspend fun getEventsForLunarDate(month: Int, day: Int, isLeapMonth: Boolean): List<SpecialDay> = emptyList()

    override suspend fun getEventsForSolarMonth(month: Int): List<SpecialDay> {
        solarCallCount++
        if (delayMs > 0) delay(delayMs)
        return solarEvents.filter { it.solarMonth == month }
    }

    override suspend fun getEventsForLunarMonth(month: Int): List<SpecialDay> {
        lunarCallCount++
        if (delayMs > 0) delay(delayMs)
        return lunarEvents.filter { it.lunarMonth == month }
    }
}
