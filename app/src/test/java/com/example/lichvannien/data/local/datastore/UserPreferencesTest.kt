package com.example.lichvannien.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dataStoreScope = CoroutineScope(testDispatcher + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tmpFolder.newFolder(), "test_user_prefs.preferences_pb") }
        )
        userPreferences = UserPreferences(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun testDefaultBirthdayIsZero() = runTest(testDispatcher) {
        val birthday = userPreferences.birthdayFlow.first()
        assertThat(birthday.day).isEqualTo(0)
        assertThat(birthday.month).isEqualTo(0)
        assertThat(birthday.year).isEqualTo(0)
        assertThat(birthday.isConfigured).isFalse()
    }

    @Test
    fun testSaveAndGetBirthday() = runTest(testDispatcher) {
        userPreferences.saveBirthday(15, 8, 1995)
        val birthday = userPreferences.birthdayFlow.first()
        assertThat(birthday.day).isEqualTo(15)
        assertThat(birthday.month).isEqualTo(8)
        assertThat(birthday.year).isEqualTo(1995)
        assertThat(birthday.isConfigured).isTrue()
    }
}
