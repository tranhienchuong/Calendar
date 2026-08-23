package com.example.lichvannien.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.DayDetail
import com.example.lichvannien.domain.model.SolarDate
import com.example.lichvannien.domain.usecase.GetDayDetailUseCase
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val getDayDetailUseCase: GetDayDetailUseCase,
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val year = savedStateHandle.get<Int>("year") ?: 0
    private val month = savedStateHandle.get<Int>("month") ?: 0
    private val day = savedStateHandle.get<Int>("day") ?: 0

    // Compute initial fast state synchronously so UI displays immediately with 0 latency
    private val initialDayDetail: DayDetail? = if (year != 0 && month != 0 && day != 0) {
        val solarDate = SolarDate(year, month, day)
        val lunarDate = lunarConverter.solarToLunar(year, month, day)
        val auspicious = auspiciousCalculator.calculate(lunarDate)
        DayDetail(
            solarDate = solarDate,
            lunarDate = lunarDate,
            auspicious = auspicious,
            specialDays = emptyList()
        )
    } else null

    private val _dayDetail = MutableStateFlow<DayDetail?>(initialDayDetail)
    val dayDetail: StateFlow<DayDetail?> = _dayDetail.asStateFlow()

    init {
        loadDayDetail(year, month, day)
    }

    private fun loadDayDetail(year: Int, month: Int, day: Int) {
        if (year == 0 || month == 0 || day == 0) {
            Log.e("DayDetailViewModel", "Invalid navigation arguments: year=$year, month=$month, day=$day")
            return
        }
        viewModelScope.launch {
            try {
                _dayDetail.value = getDayDetailUseCase(year, month, day)
            } catch (e: Exception) {
                Log.e("DayDetailViewModel", "Error fetching day details for $day/$month/$year", e)
            }
        }
    }
}
