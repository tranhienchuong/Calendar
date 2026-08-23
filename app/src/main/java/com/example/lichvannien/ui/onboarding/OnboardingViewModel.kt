package com.example.lichvannien.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.ZodiacSign
import com.example.lichvannien.domain.util.ZodiacHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedMonth = MutableStateFlow(1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _zodiacSign = MutableStateFlow<ZodiacSign?>(ZodiacHelper.getZodiacSign(1, 1))
    val zodiacSign: StateFlow<ZodiacSign?> = _zodiacSign.asStateFlow()

    private val _isInvalidDate = MutableStateFlow(false)
    val isInvalidDate: StateFlow<Boolean> = _isInvalidDate.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun updateBirthday(day: Int, month: Int) {
        _selectedDay.value = day
        _selectedMonth.value = month

        if (ZodiacHelper.isValidDate(day, month)) {
            _isInvalidDate.value = false
            _zodiacSign.value = ZodiacHelper.getZodiacSign(day, month)
        } else {
            _isInvalidDate.value = true
            _zodiacSign.value = null
        }
    }

    fun saveAndContinue() {
        val day = _selectedDay.value
        val month = _selectedMonth.value

        if (!ZodiacHelper.isValidDate(day, month)) {
            _isInvalidDate.value = true
            return
        }

        viewModelScope.launch {
            userPreferences.saveBirthday(day, month)
            _isOnboardingCompleted.value = true
        }
    }
}
