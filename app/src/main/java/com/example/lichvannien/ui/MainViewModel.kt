package com.example.lichvannien.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.domain.model.UserBirthday
import com.example.lichvannien.domain.usecase.GetBirthdayUseCase
import com.example.lichvannien.domain.usecase.SaveBirthdayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getBirthdayUseCase: GetBirthdayUseCase,
    private val saveBirthdayUseCase: SaveBirthdayUseCase
) : ViewModel() {

    val birthdayState: StateFlow<UserBirthday?> = getBirthdayUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveBirthday(day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            saveBirthdayUseCase(day, month, year)
        }
    }
}
