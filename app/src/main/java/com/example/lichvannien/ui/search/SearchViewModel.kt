package com.example.lichvannien.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.AuspiciousResult
import com.example.lichvannien.domain.model.LunarDate
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val specialDays: List<SpecialDay> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val quickDateSolar: SolarDate? = null,
    val quickDateLunar: LunarDate? = null,
    val quickDateAuspicious: AuspiciousResult? = null,
    val popularHolidays: List<SpecialDay> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val specialDayRepository: SpecialDayRepository,
    private val taskRepository: TaskRepository,
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPopularHolidays()
        val today = LocalDate.now()
        lookupSolarDate(today.year, today.monthValue, today.dayOfMonth)
    }

    private fun loadPopularHolidays() {
        viewModelScope.launch {
            try {
                val all = specialDayRepository.getAllSpecialDays()
                val popularKeywords = listOf("Tết", "Giỗ", "Quốc khánh", "Giải phóng", "Lao động", "Phụ nữ", "Nhà giáo", "Vu Lan", "Trung thu")
                val filtered = all.filter { holiday ->
                    popularKeywords.any { holiday.name.contains(it, ignoreCase = true) }
                }.distinctBy { it.name }.take(8)
                _uiState.update { it.copy(popularHolidays = if (filtered.isNotEmpty()) filtered else all.take(8)) }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        val q = newQuery.trim()
        if (q.isBlank()) {
            _uiState.update { it.copy(specialDays = emptyList(), tasks = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            try {
                val specialDays = specialDayRepository.searchSpecialDays(q)
                val tasks = taskRepository.searchTasksSync(q)
                _uiState.update { it.copy(specialDays = specialDays, tasks = tasks) }
                tryParseDateQuery(q)
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    private fun tryParseDateQuery(text: String) {
        val parts = text.split("/", "-", ".", " ")
        if (parts.size in 2..3) {
            val day = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            val year = if (parts.size == 3) parts[2].toIntOrNull() ?: LocalDate.now().year else LocalDate.now().year
            if (day != null && month != null && day in 1..31 && month in 1..12) {
                lookupSolarDate(year, month, day)
            }
        }
    }

    fun lookupSolarDate(year: Int, month: Int, day: Int) {
        try {
            val solar = SolarDate(year, month, day)
            val lunar = lunarConverter.solarToLunar(year, month, day)
            val auspicious = auspiciousCalculator.calculate(lunar)
            _uiState.update {
                it.copy(
                    quickDateSolar = solar,
                    quickDateLunar = lunar,
                    quickDateAuspicious = auspicious
                )
            }
        } catch (e: Exception) {
            // Invalid date
        }
    }

    fun lookupLunarDate(lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeapMonth: Boolean = false) {
        try {
            val solar = lunarConverter.lunarToSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth)
            val lunar = lunarConverter.solarToLunar(solar.year, solar.month, solar.day)
            val auspicious = auspiciousCalculator.calculate(lunar)
            _uiState.update {
                it.copy(
                    quickDateSolar = solar,
                    quickDateLunar = lunar,
                    quickDateAuspicious = auspicious
                )
            }
        } catch (e: Exception) {
            // Invalid date
        }
    }
}
