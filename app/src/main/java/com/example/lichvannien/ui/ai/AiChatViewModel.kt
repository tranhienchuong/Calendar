package com.example.lichvannien.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.data.remote.api.DeepSeekApi
import com.example.lichvannien.data.remote.dto.DeepSeekChatRequest
import com.example.lichvannien.data.remote.dto.DeepSeekMessage
import com.example.lichvannien.data.remote.dto.DeepSeekStreamChunk
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.EasternFengShuiHelper
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    AI, USER
}

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val userPreferences: UserPreferences,
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val taskRepository: TaskRepository,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.AI,
                    text = "Xin chào! Tôi là Trợ lý AI Phong Thủy & Lịch Việt (DeepSeek AI). Tôi có thể giúp bạn tra cứu tử vi bản mệnh, chọn ngày giờ Hoàng Đạo, tư vấn phong thủy phương Đông hoặc hỗ trợ lên lịch trình công việc!"
                )
            )
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun onInputChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun sendMessage(text: String? = null) {
        val messageText = (text ?: _uiState.value.inputText).trim()
        if (messageText.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = messageText)
        val aiMsgId = UUID.randomUUID().toString()
        val aiPlaceholder = ChatMessage(id = aiMsgId, sender = MessageSender.AI, text = "")

        val newMessages = _uiState.value.messages + userMsg + aiPlaceholder

        _uiState.update {
            it.copy(
                messages = newMessages,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            streamDeepSeekResponse(newMessages.dropLast(1), aiMsgId)
        }
    }

    private suspend fun buildSystemPrompt(): String {
        val today = LocalDate.now()
        val lunar = lunarConverter.solarToLunar(today.year, today.monthValue, today.dayOfMonth)
        val auspicious = auspiciousCalculator.calculate(lunar)

        val birthday = userPreferences.birthdayFlow.firstOrNull()
        val birthDay = birthday?.first ?: 0
        val birthMonth = birthday?.second ?: 0
        val birthYear = if (birthday != null && birthday.third > 1900) birthday.third else 1995
        val zodiacInfo = EasternFengShuiHelper.getZodiacInfo(birthYear)

        val gioHoangDaoStr = if (auspicious.gioHoangDao.isNotEmpty()) {
            auspicious.gioHoangDao.joinToString(", ")
        } else {
            "Tý (23h-1h), Dần (3h-5h), Mão (5h-7h), Ngọ (11h-13h), Mùi (13h-15h), Dậu (17h-19h)"
        }

        val birthdayStr = if (birthDay != 0 && birthMonth != 0) "$birthDay/$birthMonth/$birthYear" else "Năm $birthYear"

        return """
Bạn là "Trợ lý AI Lịch Việt & Chuyên gia Phong Thủy Phương Đông" – cố vấn thông thái, am hiểu sâu sắc về Lịch Vạn Niên Việt Nam, Học thuyết Âm Dương Ngũ Hành, Can Chi, 12 Trực, và Trạch Cát Học.

[THÔNG TIN BẢN MỆNH NGƯỜI DÙNG]
- Ngày sinh: $birthdayStr
- Tuổi Can Chi: ${zodiacInfo.canChiYear} (${zodiacInfo.animalEmoji} ${zodiacInfo.animalName})
- Bản Mệnh Ngũ Hành: ${zodiacInfo.napAm} (Hành ${zodiacInfo.nguHanh.nameVi})
- Màu sắc tương sinh / tương hợp: ${zodiacInfo.luckyColors.joinToString(", ")}
- Quy luật ngũ hành: ${zodiacInfo.suitableElements.joinToString("; ")}

[BỐI CẢNH LỊCH NGÀY HÔM NAY]
- Dương lịch: ${today.dayOfMonth}/${today.monthValue}/${today.year}
- Âm lịch: Ngày ${lunar.day} tháng ${lunar.month} năm ${lunar.year} (${lunar.canChiYear})
- Can Chi ngày: Ngày ${lunar.canChiDay}, Tháng ${lunar.canChiMonth}
- Trạng thái ngày: ${if (auspicious.isHoangDao) "Hoàng Đạo (Ngày Tốt Đại Cát)" else "Hắc Đạo (Bình thường / Cần thận trọng)"}
- Trực trong ngày: ${auspicious.truc}
- Các khung giờ Hoàng Đạo cát lành: $gioHoangDaoStr
- Hướng xuất hành: Hỷ Thần hướng Tây Nam, Tài Thần hướng Đông Nam

[QUY TẮC PHẢN HỒI BẮT BUỘC]
1. Độ dài & Cấu trúc:
   - Trả lời súc tích, cô đọng, trọng tâm, từ 150 - 250 từ.
   - Trình bày mạch lạc, chia thành các đề mục ngắn gọn với emoji phù hợp (🔮, ⭐, 🎋, ⏰).
   - Luôn viết câu kết hoàn chỉnh, tuyệt đối KHÔNG dừng dở dang giữa câu.
2. Nội dung tư vấn:
   - Tư vấn sự tương hợp giữa Bản Mệnh người dùng (${zodiacInfo.napAm}) và ngày hôm nay.
   - Gợi ý khung giờ Hoàng Đạo và hướng cát thần cụ thể.
   - Dựa vào Trực (${auspicious.truc}) để gợi ý việc nên làm / kiêng kỵ.
   - Mang tính định hướng tích cực, khoa học phong thủy Á Đông, không mê tín dị đoan tiêu cực.
""".trimIndent()
    }

    private suspend fun streamDeepSeekResponse(historyMessages: List<ChatMessage>, aiMsgId: String) {
        val systemPrompt = buildSystemPrompt()
        val apiMessages = mutableListOf<DeepSeekMessage>()
        apiMessages.add(DeepSeekMessage(role = "system", content = systemPrompt))

        val recentMessages = historyMessages.takeLast(10)
        for (msg in recentMessages) {
            val role = if (msg.sender == MessageSender.USER) "user" else "assistant"
            apiMessages.add(DeepSeekMessage(role = role, content = msg.text))
        }

        try {
            val response = withContext(ioDispatcher) {
                var res = deepSeekApi.streamChatCompletion(
                    DeepSeekChatRequest(
                        model = "deepseek-v4-flash",
                        messages = apiMessages,
                        stream = true,
                        max_tokens = 4096
                    )
                )

                if (!res.isSuccessful && res.code() == 400) {
                    res = deepSeekApi.streamChatCompletion(
                        DeepSeekChatRequest(
                            model = "deepseek-chat",
                            messages = apiMessages,
                            stream = true,
                            max_tokens = 4096
                        )
                    )
                }
                res
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val accumulatedText = StringBuilder()
                    var lastStateUpdateTime = 0L
                    val throttleIntervalMs = 80L

                    withContext(ioDispatcher) {
                        val source = responseBody.source()
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            val trimmed = line.trim()
                            if (trimmed.startsWith("data:")) {
                                val dataStr = trimmed.removePrefix("data:").trim()
                                if (dataStr == "[DONE]") break
                                if (dataStr.isNotBlank()) {
                                    try {
                                        val chunk = json.decodeFromString<DeepSeekStreamChunk>(dataStr)
                                        val contentDelta = chunk.choices.firstOrNull()?.delta?.content
                                        if (!contentDelta.isNullOrEmpty()) {
                                            accumulatedText.append(contentDelta)
                                            val now = System.currentTimeMillis()
                                            if (now - lastStateUpdateTime >= throttleIntervalMs) {
                                                val currentText = accumulatedText.toString()
                                                _uiState.update { state ->
                                                    val updated = state.messages.map { m ->
                                                        if (m.id == aiMsgId) m.copy(text = currentText) else m
                                                    }
                                                    state.copy(messages = updated, isTyping = false)
                                                }
                                                lastStateUpdateTime = now
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore malformed chunk
                                    }
                                }
                            }
                        }

                        val finalText = accumulatedText.toString()
                        if (finalText.isNotBlank()) {
                            _uiState.update { state ->
                                val updated = state.messages.map { m ->
                                    if (m.id == aiMsgId) m.copy(text = finalText) else m
                                }
                                state.copy(messages = updated, isTyping = false)
                            }
                        }
                    }

                    if (accumulatedText.isBlank()) {
                        fallbackNonStreaming(apiMessages, aiMsgId)
                    }
                } else {
                    fallbackNonStreaming(apiMessages, aiMsgId)
                }
            } else {
                val errorText = when (response.code()) {
                    401 -> "⚠️ Lỗi xác thực: API Key của bạn không hợp lệ hoặc đã hết hạn."
                    429 -> "⚠️ Quá nhiều yêu cầu cùng lúc. Vui lòng đợi vài giây rồi thử lại."
                    else -> "⚠️ Đã xảy ra lỗi khi kết nối với AI (${response.code()}). Vui lòng thử lại sau."
                }
                updateAiMessage(aiMsgId, errorText)
            }
        } catch (e: Exception) {
            fallbackNonStreaming(apiMessages, aiMsgId)
        } finally {
            _uiState.update { it.copy(isTyping = false) }
        }
    }

    private suspend fun fallbackNonStreaming(apiMessages: List<DeepSeekMessage>, aiMsgId: String) {
        try {
            val response = withContext(ioDispatcher) {
                var res = deepSeekApi.createChatCompletion(
                    DeepSeekChatRequest(
                        model = "deepseek-v4-flash",
                        messages = apiMessages,
                        stream = false,
                        max_tokens = 4096
                    )
                )
                if (!res.isSuccessful && res.code() == 400) {
                    res = deepSeekApi.createChatCompletion(
                        DeepSeekChatRequest(
                            model = "deepseek-chat",
                            messages = apiMessages,
                            stream = false,
                            max_tokens = 4096
                        )
                    )
                }
                res
            }

            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                val finalText = if (!text.isNullOrBlank()) text.trim() else "Xin lỗi, tôi chưa nhận được câu trả lời. Bạn vui lòng thử lại nhé!"
                updateAiMessage(aiMsgId, finalText)
            } else {
                updateAiMessage(aiMsgId, "⚠️ Không thể kết nối với máy chủ AI (${response.code()}).")
            }
        } catch (e: Exception) {
            updateAiMessage(aiMsgId, "⚠️ Lỗi kết nối mạng: ${e.localizedMessage ?: "Vui lòng kiểm tra lại kết nối."}")
        }
    }

    private fun updateAiMessage(id: String, text: String) {
        _uiState.update { state ->
            val updated = state.messages.map { m ->
                if (m.id == id) m.copy(text = text) else m
            }
            state.copy(messages = updated, isTyping = false)
        }
    }
}
