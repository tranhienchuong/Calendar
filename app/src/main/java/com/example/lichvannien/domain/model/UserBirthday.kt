package com.example.lichvannien.domain.model

data class UserBirthday(
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0
) {
    val isConfigured: Boolean
        get() = day != 0 && month != 0
}
