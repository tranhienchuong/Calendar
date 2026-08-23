package com.example.lichvannien.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.DayDetail
import com.example.lichvannien.domain.usecase.GetDayDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val getDayDetailUseCase: GetDayDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _dayDetail = MutableStateFlow<DayDetail?>(null)
    val dayDetail: StateFlow<DayDetail?> = _dayDetail.asStateFlow()

    init {
        val year = savedStateHandle.get<Int>("year") ?: 0
        val month = savedStateHandle.get<Int>("month") ?: 0
        val day = savedStateHandle.get<Int>("day") ?: 0

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
