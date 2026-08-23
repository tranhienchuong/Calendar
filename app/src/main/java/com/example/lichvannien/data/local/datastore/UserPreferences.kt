package com.example.lichvannien.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        val KEY_BIRTH_DAY = intPreferencesKey("birth_day")
        val KEY_BIRTH_MONTH = intPreferencesKey("birth_month")
        val KEY_BIRTH_YEAR = intPreferencesKey("birth_year")
    }

    val birthdayFlow: Flow<Triple<Int, Int, Int>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val day = preferences[KEY_BIRTH_DAY] ?: 0
            val month = preferences[KEY_BIRTH_MONTH] ?: 0
            val year = preferences[KEY_BIRTH_YEAR] ?: 0
            Triple(day, month, year)
        }
        .distinctUntilChanged()

    suspend fun saveBirthday(day: Int, month: Int, year: Int = 0) {
        dataStore.edit { preferences ->
            preferences[KEY_BIRTH_DAY] = day
            preferences[KEY_BIRTH_MONTH] = month
            preferences[KEY_BIRTH_YEAR] = year
        }
    }
}
