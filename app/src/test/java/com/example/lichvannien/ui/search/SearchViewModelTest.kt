package com.example.lichvannien.ui.search

import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.model.SpecialDay
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakeSpecialDayRepo = FakeSpecialDayRepository()
    private val fakeTaskRepo = FakeTaskRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            specialDayRepository = fakeSpecialDayRepo,
            taskRepository = fakeTaskRepo,
            lunarConverter = LunarConverter,
            auspiciousCalculator = AuspiciousCalculator
        )
    }

    @Test
    fun initial_loadsPopularHolidays_andSetsTodayQuickLookup() = testScope.runTest {
        fakeSpecialDayRepo.allHolidays = listOf(
            SpecialDay(name = "Tết Nguyên Đán", icon = "🧧", lunarMonth = 1, lunarDay = 1, isLunar = true),
            SpecialDay(name = "Quốc khánh", icon = "🇻🇳", solarMonth = 9, solarDay = 2, isLunar = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.popularHolidays).isNotEmpty()
        assertThat(state.quickDateSolar).isNotNull()
        assertThat(state.quickDateLunar).isNotNull()
    }

    @Test
    fun queryChanged_searchesHolidaysAndTasks() = testScope.runTest {
        fakeSpecialDayRepo.allHolidays = listOf(
            SpecialDay(name = "Tết Nguyên Đán", icon = "🧧", lunarMonth = 1, lunarDay = 1, isLunar = true),
            SpecialDay(name = "Giỗ Tổ Hùng Vương", icon = "🏛️", lunarMonth = 3, lunarDay = 10, isLunar = true)
        )
        fakeTaskRepo.taskList = listOf(
            TaskEntity(id = 1, title = "Mua quà Tết", date = "2026-02-15", isCompleted = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("Tết")
        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.specialDays).hasSize(1)
        assertThat(state.specialDays[0].name).isEqualTo("Tết Nguyên Đán")
        assertThat(state.tasks).hasSize(1)
        assertThat(state.tasks[0].title).isEqualTo("Mua quà Tết")
    }

    @Test
    fun quickDateLookup_solarToLunar_computesAccurately() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // 02/09/2026
        viewModel.lookupSolarDate(2026, 9, 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.quickDateSolar?.day).isEqualTo(2)
        assertThat(state.quickDateSolar?.month).isEqualTo(9)
        assertThat(state.quickDateSolar?.year).isEqualTo(2026)
        assertThat(state.quickDateLunar).isNotNull()
        assertThat(state.quickDateAuspicious).isNotNull()
    }
}

private class FakeSpecialDayRepository : SpecialDayRepository {
    var allHolidays = listOf<SpecialDay>()

    override suspend fun getEventsForSolarDate(month: Int, day: Int): List<SpecialDay> = emptyList()
    override suspend fun getEventsForLunarDate(month: Int, day: Int, isLeapMonth: Boolean): List<SpecialDay> = emptyList()
    override suspend fun getEventsForSolarMonth(month: Int): List<SpecialDay> = emptyList()
    override suspend fun getEventsForLunarMonth(month: Int): List<SpecialDay> = emptyList()

    override suspend fun searchSpecialDays(query: String): List<SpecialDay> {
        return allHolidays.filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun getAllSpecialDays(): List<SpecialDay> = allHolidays
}

private class FakeTaskRepository : TaskRepository {
    var taskList = listOf<TaskEntity>()

    override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(taskList)
    override fun getTasksForDate(date: String): Flow<List<TaskEntity>> = flowOf(taskList.filter { it.date == date })
    override suspend fun getTasksForDateSync(date: String): List<TaskEntity> = taskList.filter { it.date == date }
    override fun searchTasks(query: String): Flow<List<TaskEntity>> = flowOf(taskList.filter { it.title.contains(query, ignoreCase = true) })
    override suspend fun searchTasksSync(query: String): List<TaskEntity> = taskList.filter { it.title.contains(query, ignoreCase = true) }
    override suspend fun addTask(task: TaskEntity): Long = 1L
    override suspend fun updateTask(task: TaskEntity) {}
    override suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean) {}
    override suspend fun deleteTask(id: Long) {}
}
