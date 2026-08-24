package com.example.lichvannien.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.EasternZodiacInfo
import com.example.lichvannien.domain.usecase.SaveBirthdayUseCase
import com.example.lichvannien.domain.util.EasternFengShuiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveBirthdayUseCase: SaveBirthdayUseCase
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfMonth)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(1995)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _zodiacInfo = MutableStateFlow<EasternZodiacInfo>(EasternFengShuiHelper.getZodiacInfo(1995))
    val zodiacInfo: StateFlow<EasternZodiacInfo> = _zodiacInfo.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun updateBirthday(day: Int, month: Int, year: Int) {
        _selectedDay.value = day
        _selectedMonth.value = month
        _selectedYear.value = year
        _zodiacInfo.value = EasternFengShuiHelper.getZodiacInfo(year)
    }

    fun saveAndContinue() {
        val day = _selectedDay.value
        val month = _selectedMonth.value
        val year = _selectedYear.value

        viewModelScope.launch {
            saveBirthdayUseCase(day, month, year)
            _isOnboardingCompleted.value = true
        }
    }
}
