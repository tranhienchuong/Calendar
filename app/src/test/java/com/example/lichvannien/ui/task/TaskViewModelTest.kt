package com.example.lichvannien.ui.task

import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.domain.repository.TaskRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val fakeRepo = FakeTaskRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TaskViewModel {
        return TaskViewModel(taskRepository = fakeRepo, reminderScheduler = null)
    }

    @Test
    fun initialState_loadsTasksAndCalculatesCounters() = testScope.runTest {
        val todayStr = LocalDate.now().toString()
        fakeRepo.tasksFlow.value = listOf(
            Task(id = 1, title = "Cắm cơm", date = todayStr, dueTime = "10:30", isCompleted = false),
            Task(id = 2, title = "Học Trade", date = todayStr, dueTime = "13:00", isCompleted = false),
            Task(id = 3, title = "Thể dục", date = todayStr, dueTime = "16:30", isCompleted = true)
        )

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.totalCount).isEqualTo(3)
        assertThat(state.pendingCount).isEqualTo(2)
        assertThat(state.completedCount).isEqualTo(1)
        assertThat(state.displayedTasks).hasSize(3)

        collectJob.cancel()
    }

    @Test
    fun filter_pending_showsOnlyUncompletedTasks() = testScope.runTest {
        val todayStr = LocalDate.now().toString()
        fakeRepo.tasksFlow.value = listOf(
            Task(id = 1, title = "Task 1", date = todayStr, isCompleted = false),
            Task(id = 2, title = "Task 2", date = todayStr, isCompleted = true)
        )

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.setFilter(TaskFilter.PENDING)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.displayedTasks).hasSize(1)
        assertThat(state.displayedTasks[0].id).isEqualTo(1L)

        collectJob.cancel()
    }

    @Test
    fun searchQuery_filtersByTitle() = testScope.runTest {
        fakeRepo.tasksFlow.value = listOf(
            Task(id = 1, title = "Học lập trình Kotlin", date = "2026-08-24", isCompleted = false),
            Task(id = 2, title = "Đi siêu thị mua hoa quả", date = "2026-08-24", isCompleted = false)
        )

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.setSearchQuery("Kotlin")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.displayedTasks).hasSize(1)
        assertThat(state.displayedTasks[0].title).contains("Kotlin")

        collectJob.cancel()
    }

    @Test
    fun toggleTask_callsRepositoryToggle() = testScope.runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.toggleTask(id = 5L, isCompleted = true)
        advanceUntilIdle()

        assertThat(fakeRepo.lastToggledId).isEqualTo(5L)
        assertThat(fakeRepo.lastToggledCompleted).isTrue()

        collectJob.cancel()
    }

    @Test
    fun toggleTask_keepsTaskCompletedToday() = testScope.runTest {
        val todayStr = LocalDate.now().toString()
        val dailyTask = Task(
            id = 10L,
            title = "Uống nước",
            date = todayStr,
            repeatType = "DAILY",
            isCompleted = false
        )
        fakeRepo.tasksFlow.value = listOf(dailyTask)

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.toggleTask(id = 10L, isCompleted = true)
        advanceUntilIdle()

        assertThat(fakeRepo.lastToggledId).isEqualTo(10L)
        assertThat(fakeRepo.lastToggledCompleted).isTrue()

        collectJob.cancel()
    }

    @Test
    fun refreshRecurringTasks_pastCompletedDailyTask_refreshesToTodayUncompleted() = testScope.runTest {
        val yesterdayStr = "2026-08-23"
        val today = LocalDate.of(2026, 8, 24)
        val dailyTask = Task(
            id = 10L,
            title = "Uống nước",
            date = yesterdayStr,
            repeatType = "DAILY",
            isCompleted = true
        )
        fakeRepo.tasksFlow.value = listOf(dailyTask)

        val viewModel = createViewModel()
        viewModel.refreshRecurringTasks(today)
        advanceUntilIdle()

        assertThat(fakeRepo.lastUpdatedTask).isNotNull()
        assertThat(fakeRepo.lastUpdatedTask?.id).isEqualTo(10L)
        assertThat(fakeRepo.lastUpdatedTask?.isCompleted).isFalse()
        assertThat(fakeRepo.lastUpdatedTask?.date).isEqualTo("2026-08-24")
    }
}

private class FakeTaskRepository : TaskRepository {
    val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    var lastToggledId: Long? = null
    var lastToggledCompleted: Boolean? = null
    var lastUpdatedTask: Task? = null
    var lastDeletedId: Long? = null

    override fun getAllTasks(): Flow<List<Task>> = tasksFlow
    override fun getTasksForDate(date: String): Flow<List<Task>> = tasksFlow
    override suspend fun getTasksForDateSync(date: String): List<Task> = tasksFlow.value
    override fun searchTasks(query: String): Flow<List<Task>> = tasksFlow
    override suspend fun searchTasksSync(query: String): List<Task> = tasksFlow.value
    override suspend fun getTaskById(id: Long): Task? = tasksFlow.value.find { it.id == id }
    override suspend fun getRecurringTasks(): List<Task> = tasksFlow.value.filter { it.repeatType != "ONCE" }
    override suspend fun addTask(task: Task): Long = 1L
    override suspend fun updateTask(task: Task) {
        lastUpdatedTask = task
    }
    override suspend fun updateTasks(tasks: List<Task>) {
        if (tasks.isNotEmpty()) {
            lastUpdatedTask = tasks.last()
        }
    }
    override suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean) {
        lastToggledId = id
        lastToggledCompleted = isCompleted
    }
    override suspend fun deleteTask(id: Long) {
        lastDeletedId = id
    }
}
