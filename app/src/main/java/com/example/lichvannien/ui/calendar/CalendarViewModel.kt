package com.example.lichvannien.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.CalendarDay
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val specialDayRepository: SpecialDayRepository
) : ViewModel() {

    private val _currentMonthYear = MutableStateFlow(YearMonth.now())
    val currentMonthYear: StateFlow<YearMonth> = _currentMonthYear.asStateFlow()

    private val _daysList = MutableStateFlow<ImmutableList<CalendarDay>>(persistentListOf())
    val daysList: StateFlow<ImmutableList<CalendarDay>> = _daysList.asStateFlow()

    init {
        val today = YearMonth.now()
        loadMonth(today.year, today.monthValue)
    }

    fun loadMonth(year: Int, month: Int) {
        viewModelScope.launch {
            // 1. Tải toàn bộ sự kiện dương lịch trong tháng này
            val solarMonthEvents = specialDayRepository.getEventsForSolarMonth(month)

            // 2. Tính toán khoảng tháng âm lịch chồng lấn để tải sự kiện âm lịch tương ứng
            val firstDayOfMonth = LocalDate.of(year, month, 1)
            val lastDayOfMonthVal = YearMonth.of(year, month).lengthOfMonth()
            
            // Tính ngày âm lịch đầu tháng và cuối tháng dương
            val firstLunar = withContext(Dispatchers.Default) {
                lunarConverter.solarToLunar(year, month, 1)
            }
            val lastLunar = withContext(Dispatchers.Default) {
                lunarConverter.solarToLunar(year, month, lastDayOfMonthVal)
            }

            val lunarMonthStart = firstLunar.month
            val lunarMonthEnd = lastLunar.month

            // Tải sự kiện âm lịch của các tháng âm lịch có thể xuất hiện trong tháng dương lịch này
            val lunarEventsStart = specialDayRepository.getEventsForLunarMonth(lunarMonthStart)
            val lunarEventsEnd = if (lunarMonthEnd != lunarMonthStart) {
                specialDayRepository.getEventsForLunarMonth(lunarMonthEnd)
            } else {
                emptyList()
            }
            val allLunarEvents = lunarEventsStart + lunarEventsEnd

            // 3. Tính toán lưới 42 ngày (6 tuần)
            val dayOfWeekVal = firstDayOfMonth.dayOfWeek.value // Thứ 2 = 1, Chủ Nhật = 7
            val startOffset = dayOfWeekVal - 1 // T2 = 0, CN = 6
            val gridStartDate = firstDayOfMonth.minusDays(startOffset.toLong())

            val days = withContext(Dispatchers.Default) {
                (0 until 42).map { i ->
                    val date = gridStartDate.plusDays(i.toLong())
                    val isCurrentMonth = date.monthValue == month && date.year == year
                    val isToday = date == LocalDate.now()
                    val solar = SolarDate(date.year, date.monthValue, date.dayOfMonth)

                    if (isCurrentMonth) {
                        val lunar = lunarConverter.solarToLunar(date.year, date.monthValue, date.dayOfMonth)
                        val rating = auspiciousCalculator.calculate(lunar)

                        // Kiểm tra sự kiện
                        val hasSolarEvent = solarMonthEvents.any { it.solarDay == date.dayOfMonth }
                        val hasLunarEvent = allLunarEvents.any {
                            it.lunarMonth == lunar.month && it.lunarDay == lunar.day && it.leapMonth == lunar.isLeapMonth
                        }

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
            }

            _daysList.value = days.toImmutableList()
            _currentMonthYear.value = YearMonth.of(year, month)
        }
    }

    fun goToPreviousMonth() {
        val prev = _currentMonthYear.value.minusMonths(1)
        loadMonth(prev.year, prev.monthValue)
    }

    fun goToNextMonth() {
        val next = _currentMonthYear.value.plusMonths(1)
        loadMonth(next.year, next.monthValue)
    }

    fun goToToday() {
        val today = YearMonth.now()
        loadMonth(today.year, today.monthValue)
    }
}
