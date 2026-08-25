package com.example.lichvannien.ui.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@Immutable
data class CalendarDayUiModel(
    val year: Int,
    val month: Int,
    val day: Int,
    val solarDayText: String,
    val lunarDayText: String?,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSaturday: Boolean,
    val isSunday: Boolean,
    val isHoangDao: Boolean,
    val hasSpecialEvent: Boolean,
    val contentDescription: String,
    val lunarSubLabel: String? = null
)

data class CalendarUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val targetMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysList: ImmutableList<CalendarDayUiModel> = persistentListOf(),
    val monthDataMap: Map<YearMonth, ImmutableList<CalendarDayUiModel>> = emptyMap(),
    val selectedDateTasks: List<Task> = emptyList(),
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
    private val taskRepository: TaskRepository? = null,
    private val reminderScheduler: com.example.lichvannien.ui.task.reminder.TaskReminderScheduler? = null,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    companion object {
        private const val MAX_CACHE_SIZE = 48
    }

    private val monthCache = object : LinkedHashMap<YearMonth, ImmutableList<CalendarDayUiModel>>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<YearMonth, ImmutableList<CalendarDayUiModel>>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    private fun getCachedMonth(yearMonth: YearMonth): ImmutableList<CalendarDayUiModel>? {
        return synchronized(monthCache) {
            monthCache[yearMonth]
        }
    }

    private fun putCachedMonth(yearMonth: YearMonth, days: ImmutableList<CalendarDayUiModel>) {
        synchronized(monthCache) {
            monthCache[yearMonth] = days
        }
    }

    private fun getCacheSnapshot(): Map<YearMonth, ImmutableList<CalendarDayUiModel>> {
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

    val daysList: StateFlow<ImmutableList<CalendarDayUiModel>> = _uiState
        .map { it.daysList }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _uiState.value.daysList
        )

    private var activeLoadJob: Job? = null
    private var preloadJob: Job? = null
    private val inFlightMonths = java.util.Collections.synchronizedSet(HashSet<YearMonth>())

    private var taskJob: Job? = null

    init {
        val initialMonth = YearMonth.now()
        navigateToMonth(initialMonth)
        observeTasksForDate(LocalDate.now())
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        observeTasksForDate(date)
    }

    private fun observeTasksForDate(date: LocalDate) {
        taskJob?.cancel()
        val repo = taskRepository ?: return
        taskJob = viewModelScope.launch {
            val dateStr = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            repo.getTasksForDate(dateStr).collect { tasks ->
                _uiState.update { it.copy(selectedDateTasks = tasks) }
            }
        }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        val repo = taskRepository ?: return
        viewModelScope.launch {
            repo.toggleTaskCompleted(id, isCompleted)
            val task = repo.getTaskById(id)
            if (task != null) {
                if (isCompleted && task.repeatType.equals("ONCE", ignoreCase = true)) {
                    reminderScheduler?.cancelTaskReminder(id)
                } else {
                    reminderScheduler?.scheduleTaskReminder(task.copy(isCompleted = isCompleted))
                }
            }
        }
    }

    fun addNewTask(title: String, startTime: String?, endTime: String?, location: String?) {
        val repo = taskRepository ?: return
        val date = _uiState.value.selectedDate
        val dateStr = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val finalDueTime = startTime?.ifBlank { null }
        viewModelScope.launch {
            val task = Task(
                title = title,
                date = dateStr,
                startTime = finalDueTime,
                dueTime = finalDueTime,
                endTime = endTime?.ifBlank { null },
                location = location?.ifBlank { null },
                isCompleted = false,
                category = "WORK",
                colorHex = 0xFF1976D2
            )
            val generatedId = repo.addTask(task)
            if (generatedId > 0) {
                reminderScheduler?.scheduleTaskReminder(task.copy(id = generatedId))
            }
        }
    }

    fun getMonthDays(yearMonth: YearMonth): ImmutableList<CalendarDayUiModel>? {
        val cached = getCachedMonth(yearMonth)
        if (cached != null) return cached

        if (inFlightMonths.add(yearMonth)) {
            viewModelScope.launch(defaultDispatcher) {
                try {
                    val days = generateMonthDays(yearMonth)
                    putCachedMonth(yearMonth, days)
                    _uiState.update {
                        it.copy(monthDataMap = getCacheSnapshot())
                    }
                } finally {
                    inFlightMonths.remove(yearMonth)
                }
            }
        }
        return null
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
        preloadJob?.cancel()
        preloadJob = viewModelScope.launch(defaultDispatcher) {
            var updated = false
            // Preload 3 months in both directions: -1, +1, -2, +2, -3, +3
            for (offset in listOf(-1, 1, -2, 2, -3, 3)) {
                val targetMonth = baseMonth.plusMonths(offset.toLong())
                if (getCachedMonth(targetMonth) == null) {
                    val days = generateMonthDays(targetMonth)
                    putCachedMonth(targetMonth, days)
                    updated = true
                }
            }
            if (updated) {
                _uiState.update {
                    it.copy(monthDataMap = getCacheSnapshot())
                }
            }
        }
    }

    private suspend fun generateMonthDays(yearMonth: YearMonth): ImmutableList<CalendarDayUiModel> = withContext(defaultDispatcher) {
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
            val dayOfWeek = date.dayOfWeek
            val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
            val isSunday = dayOfWeek == DayOfWeek.SUNDAY
            val solarDayText = date.dayOfMonth.toString()

            if (isCurrentMonth) {
                val lunar = lunarDates[date.dayOfMonth] ?: lunarConverter.solarToLunar(date.year, date.monthValue, date.dayOfMonth)
                val rating = auspiciousCalculator.calculate(lunar)

                val hasSolarEvent = date.dayOfMonth in solarEventDays
                val hasLunarEvent = LunarEventKey(lunar.month, lunar.day, lunar.isLeapMonth) in lunarEventKeys
                val hasSpecialEvent = hasSolarEvent || hasLunarEvent

                val lunarDayText = "${lunar.day}/${lunar.month}"
                val lunarSubLabel = when (lunar.day) {
                    1 -> "Mùng Một"
                    15 -> "Rằm T${lunar.month}"
                    else -> null
                }

                val contentDescription = buildContentDescription(
                    dayOfWeek = dayOfWeek,
                    solarDay = date.dayOfMonth,
                    solarMonth = date.monthValue,
                    solarYear = date.year,
                    lunarMonth = lunar.month,
                    lunarDay = lunar.day,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isHoangDao = rating.isHoangDao,
                    hasSpecialEvent = hasSpecialEvent
                )

                CalendarDayUiModel(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth,
                    solarDayText = solarDayText,
                    lunarDayText = lunarDayText,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSaturday = isSaturday,
                    isSunday = isSunday,
                    isHoangDao = rating.isHoangDao,
                    hasSpecialEvent = hasSpecialEvent,
                    contentDescription = contentDescription,
                    lunarSubLabel = lunarSubLabel
                )
            } else {
                val contentDescription = buildContentDescription(
                    dayOfWeek = dayOfWeek,
                    solarDay = date.dayOfMonth,
                    solarMonth = date.monthValue,
                    solarYear = date.year,
                    lunarMonth = null,
                    lunarDay = null,
                    isCurrentMonth = false,
                    isToday = isToday,
                    isHoangDao = false,
                    hasSpecialEvent = false
                )

                CalendarDayUiModel(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth,
                    solarDayText = solarDayText,
                    lunarDayText = null,
                    isCurrentMonth = false,
                    isToday = isToday,
                    isSaturday = isSaturday,
                    isSunday = isSunday,
                    isHoangDao = false,
                    hasSpecialEvent = false,
                    contentDescription = contentDescription
                )
            }
        }

        days.toImmutableList()
    }

    private fun buildContentDescription(
        dayOfWeek: DayOfWeek,
        solarDay: Int,
        solarMonth: Int,
        solarYear: Int,
        lunarMonth: Int?,
        lunarDay: Int?,
        isCurrentMonth: Boolean,
        isToday: Boolean,
        isHoangDao: Boolean,
        hasSpecialEvent: Boolean
    ): String {
        return buildString {
            if (isToday) {
                append("Hôm nay, ")
            }
            val dayOfWeekName = when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Thứ Hai"
                DayOfWeek.TUESDAY -> "Thứ Ba"
                DayOfWeek.WEDNESDAY -> "Thứ Tư"
                DayOfWeek.THURSDAY -> "Thứ Năm"
                DayOfWeek.FRIDAY -> "Thứ Sáu"
                DayOfWeek.SATURDAY -> "Thứ Bảy"
                DayOfWeek.SUNDAY -> "Chủ Nhật"
            }
            append("$dayOfWeekName, ngày $solarDay tháng $solarMonth năm $solarYear. ")
            if (lunarDay != null && lunarMonth != null) {
                append("Âm lịch ngày $lunarDay tháng $lunarMonth. ")
            }
            if (isCurrentMonth) {
                if (isHoangDao) {
                    append("Ngày Hoàng Đạo. ")
                } else {
                    append("Ngày Hắc Đạo. ")
                }
            }
            if (hasSpecialEvent) {
                append("Có sự kiện đặc biệt. ")
            }
        }
    }
}
