package com.example.lichvannien.ui.ai

import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.data.remote.api.DeepSeekApi
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val deepSeekApi: DeepSeekApi = mockk()
    private val userPreferences: UserPreferences = mockk()
    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val lunarConverter = LunarConverter
    private val auspiciousCalculator = AuspiciousCalculator
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var viewModel: AiChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferences.birthdayFlow } returns flowOf(Triple(15, 8, 1995))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendMessage_callsDeepSeekApiAndUpdatesState() = runTest(testDispatcher) {
        val sseStream = """
            data: {"choices":[{"delta":{"content":"Tử vi hôm nay cho tuổi Ất Hợi "}}]}
            data: {"choices":[{"delta":{"content":"rất tốt."}}]}
            data: [DONE]
        """.trimIndent()

        val responseBody = sseStream.toResponseBody("text/event-stream".toMediaType())
        coEvery { deepSeekApi.streamChatCompletion(any()) } returns Response.success(responseBody)

        viewModel = AiChatViewModel(
            deepSeekApi = deepSeekApi,
            userPreferences = userPreferences,
            lunarConverter = lunarConverter,
            auspiciousCalculator = auspiciousCalculator,
            taskRepository = taskRepository,
            json = json,
            ioDispatcher = testDispatcher
        )

        viewModel.onInputChanged("Hôm nay tuổi Ất Hợi thế nào?")
        viewModel.sendMessage()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.messages).isNotEmpty()
        val lastMessage = state.messages.last()
        assertThat(lastMessage.sender).isEqualTo(MessageSender.AI)
        assertThat(lastMessage.text).contains("Tử vi hôm nay cho tuổi Ất Hợi rất tốt.")
        assertThat(state.isTyping).isFalse()
    }
}
