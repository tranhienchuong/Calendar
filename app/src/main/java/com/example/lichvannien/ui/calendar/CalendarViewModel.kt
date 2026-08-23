package com.example.lichvannien.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.CalendarDay
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import com.example.lichvannien.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val targetMonth: YearMonth = YearMonth.now(),
    val daysList: ImmutableList<CalendarDay> = persistentListOf(),
    val monthDataMap: Map<YearMonth, ImmutableList<CalendarDay>> = emptyMap(),
    val isLoading: Boolean = false
)

private data class LunarEventKey(
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val specialDayRepository: SpecialDayRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    companion object {
        private const val MAX_CACHE_SIZE = 12
    }

    private val monthCache = object : LinkedHashMap<YearMonth, ImmutableList<CalendarDay>>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<YearMonth, ImmutableList<CalendarDay>>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    private fun getCachedMonth(yearMonth: YearMonth): ImmutableList<CalendarDay>? {
        return synchronized(monthCache) {
            monthCache[yearMonth]
        }
    }

    private fun putCachedMonth(yearMonth: YearMonth, days: ImmutableList<CalendarDay>) {
        synchronized(monthCache) {
            monthCache[yearMonth] = days
        }
    }

    private fun getCacheSnapshot(): Map<YearMonth, ImmutableList<CalendarDay>> {
        return synchronized(monthCache) {
            HashMap(monthCache)
        }
    }

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            displayedMonth = YearMonth.now(),
            targetMonth = YearMonth.now(),
            daysList = persistentListOf(),
            monthDataMap = emptyMap(),
            isLoading = true
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Backwards-compatible StateFlows for existing consumers/tests
    val currentMonthYear: StateFlow<YearMonth> = _uiState
        .map { it.targetMonth }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _uiState.value.targetMonth
        )

    val daysList: StateFlow<ImmutableList<CalendarDay>> = _uiState
        .map { it.daysList }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _uiState.value.daysList
        )

    private var activeLoadJob: Job? = null

    init {
        val initialMonth = YearMonth.now()
        navigateToMonth(initialMonth)
    }

    fun getMonthDays(yearMonth: YearMonth): ImmutableList<CalendarDay>? {
        val cached = getCachedMonth(yearMonth)
        if (cached == null) {
            viewModelScope.launch(defaultDispatcher) {
                val days = generateMonthDays(yearMonth)
                putCachedMonth(yearMonth, days)
                _uiState.update {
                    it.copy(monthDataMap = getCacheSnapshot())
                }
            }
        }
        return cached
    }

    fun loadMonth(year: Int, month: Int) {
        navigateToMonth(YearMonth.of(year, month))
    }

    fun goToPreviousMonth() {
        val prev = _uiState.value.targetMonth.minusMonths(1)
        navigateToMonth(prev)
    }

    fun goToNextMonth() {
        val next = _uiState.value.targetMonth.plusMonths(1)
        navigateToMonth(next)
    }

    fun goToToday() {
        val today = YearMonth.now()
        navigateToMonth(today)
    }

    private fun navigateToMonth(target: YearMonth) {
        val cached = getCachedMonth(target)
        if (cached != null) {
            activeLoadJob?.cancel()
            activeLoadJob = null
            _uiState.update {
                it.copy(
                    targetMonth = target,
                    displayedMonth = target,
                    daysList = cached,
                    monthDataMap = getCacheSnapshot(),
                    isLoading = false
                )
            }
            preloadAdjacentMonths(target)
        } else {
            _uiState.update {
                it.copy(
                    targetMonth = target,
                    isLoading = true
                )
            }
            activeLoadJob?.cancel()
            activeLoadJob = viewModelScope.launch(defaultDispatcher) {
                val days = generateMonthDays(target)
                putCachedMonth(target, days)
                if (_uiState.value.targetMonth == target) {
                    _uiState.update {
                        it.copy(
                            displayedMonth = target,
                            daysList = days,
                            monthDataMap = getCacheSnapshot(),
                            isLoading = false
                        )
                    }
                    preloadAdjacentMonths(target)
                }
            }
        }
    }

    private fun preloadAdjacentMonths(baseMonth: YearMonth) {
        viewModelScope.launch(defaultDispatcher) {
            val prevMonth = baseMonth.minusMonths(1)
            val nextMonth = baseMonth.plusMonths(1)
            var updated = false

            if (getCachedMonth(prevMonth) == null) {
                val prevDays = generateMonthDays(prevMonth)
                putCachedMonth(prevMonth, prevDays)
                updated = true
            }
            if (getCachedMonth(nextMonth) == null) {
                val nextDays = generateMonthDays(nextMonth)
                putCachedMonth(nextMonth, nextDays)
                updated = true
            }
            if (updated) {
                _uiState.update {
                    it.copy(monthDataMap = getCacheSnapshot())
                }
            }
        }
    }

    private suspend fun generateMonthDays(yearMonth: YearMonth): ImmutableList<CalendarDay> = withContext(defaultDispatcher) {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        val lengthOfMonth = yearMonth.lengthOfMonth()
        val firstDayOfMonth = LocalDate.of(year, month, 1)

        // 1. Calculate lunar dates for all days in this solar month
        val lunarDates = (1..lengthOfMonth).associateWith { day ->
            lunarConverter.solarToLunar(year, month, day)
        }

        // 2. Determine unique lunar months occurring in this solar month
        val distinctLunarMonths = lunarDates.values.map { it.month }.toSet()

        // 3. Fetch solar events for this month
        val solarMonthEvents = specialDayRepository.getEventsForSolarMonth(month)
        val solarEventDays: Set<Int> = solarMonthEvents.mapNotNull { it.solarDay }.toSet()

        // 4. Fetch lunar events for all relevant lunar months
        val allLunarEvents = distinctLunarMonths.flatMap { lunarMonth ->
            specialDayRepository.getEventsForLunarMonth(lunarMonth)
        }
        val lunarEventKeys: Set<LunarEventKey> = allLunarEvents.mapNotNull { event ->
            val lm = event.lunarMonth
            val ld = event.lunarDay
            if (lm != null && ld != null) {
                LunarEventKey(lm, ld, event.leapMonth)
            } else null
        }.toSet()

        // 5. Grid calculation (6 weeks = 42 cells)
        val dayOfWeekVal = firstDayOfMonth.dayOfWeek.value // Mon = 1, Sun = 7
        val startOffset = dayOfWeekVal - 1 // Mon = 0, Sun = 6
        val gridStartDate = firstDayOfMonth.minusDays(startOffset.toLong())
        val today = LocalDate.now()

        val days = (0 until 42).map { i ->
            val date = gridStartDate.plusDays(i.toLong())
            val isCurrentMonth = date.monthValue == month && date.year == year
            val isToday = date == today
            val solar = SolarDate(date.year, date.monthValue, date.dayOfMonth)

            if (isCurrentMonth) {
                val lunar = lunarDates[date.dayOfMonth] ?: lunarConverter.solarToLunar(date.year, date.monthValue, date.dayOfMonth)
                val rating = auspiciousCalculator.calculate(lunar)

                val hasSolarEvent = date.dayOfMonth in solarEventDays
                val hasLunarEvent = LunarEventKey(lunar.month, lunar.day, lunar.isLeapMonth) in lunarEventKeys

                CalendarDay(
                    solarDate = solar,
                    lunarDate = lunar,
                    isHoangDao = rating.isHoangDao,
                    hasSpecialEvent = hasSolarEvent || hasLunarEvent,
                    isCurrentMonth = true,
                    isToday = isToday
                )
            } else {
                CalendarDay(
                    solarDate = solar,
                    lunarDate = null,
                    isHoangDao = false,
                    hasSpecialEvent = false,
                    isCurrentMonth = false,
                    isToday = isToday
                )
            }
        }

        days.toImmutableList()
    }
}
