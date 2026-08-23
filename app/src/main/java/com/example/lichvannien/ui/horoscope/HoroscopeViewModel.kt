package com.example.lichvannien.ui.horoscope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.model.Horoscope
import com.example.lichvannien.domain.model.ZodiacSign
import com.example.lichvannien.domain.repository.HoroscopeRepository
import com.example.lichvannien.domain.util.ZodiacHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HoroscopeTab(val value: String) {
    TODAY("today"),
    WEEK("week"),
    MONTH("month")
}

@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    userPreferences: UserPreferences,
    private val horoscopeRepository: HoroscopeRepository
) : ViewModel() {

    val zodiacSign: StateFlow<ZodiacSign?> = userPreferences.birthdayFlow
        .map { (day, month) ->
            ZodiacHelper.getZodiacSign(day, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _selectedTab = MutableStateFlow(HoroscopeTab.TODAY)
    val selectedTab: StateFlow<HoroscopeTab> = _selectedTab.asStateFlow()

    private val _horoscope = MutableStateFlow<Horoscope?>(null)
    val horoscope: StateFlow<Horoscope?> = _horoscope.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            zodiacSign.collect { sign ->
                if (sign != null) {
                    loadHoroscope()
                }
            }
        }
    }

    fun loadHoroscope() {
        viewModelScope.launch {
            val sign = zodiacSign.value?.nameEn ?: ""
            if (sign.isEmpty()) return@launch

            _isLoading.value = true
            _error.value = null
            
            // Độ trễ nạp tối thiểu ~300ms để hiển thị hiệu ứng Shimmer mượt mà khi chuyển Tab
            delay(300)

            val tabVal = _selectedTab.value.value
            horoscopeRepository.getHoroscope(sign, tabVal)
                .onSuccess {
                    _horoscope.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "Failed to load horoscope"
                }
            _isLoading.value = false
        }
    }

    fun onTabSelected(tab: HoroscopeTab) {
        _selectedTab.value = tab
        loadHoroscope()
    }

    fun refresh() {
        loadHoroscope()
    }
}
