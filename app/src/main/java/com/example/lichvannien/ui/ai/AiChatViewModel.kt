package com.example.lichvannien.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lichvannien.data.local.datastore.UserPreferences
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.EasternFengShuiHelper
import com.example.lichvannien.domain.util.LunarConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
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
    private val userPreferences: UserPreferences,
    private val lunarConverter: LunarConverter,
    private val auspiciousCalculator: AuspiciousCalculator,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.AI,
                    text = "Xin chào! Tôi là trợ lý AI Phong Thủy & Lịch Âm Dương của bạn. Tôi có thể giúp bạn tra cứu Can Chi, Mệnh Ngũ Hành, chọn ngày giờ Hoàng Đạo hoặc hỗ trợ sắp xếp lịch trình công việc!"
                ),
                ChatMessage(
                    sender = MessageSender.USER,
                    text = "Lập lịch cho tôi một cuộc họp vào thứ Hai tới lúc 10h sáng."
                ),
                ChatMessage(
                    sender = MessageSender.AI,
                    text = "Tuyệt vời! Tôi đã thêm cuộc họp vào thứ Hai lúc 10h sáng. Bạn muốn thêm tên cuộc họp hoặc địa điểm không?"
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
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            delay(800)
            val aiResponse = generateAiResponse(messageText)
            val aiMsg = ChatMessage(sender = MessageSender.AI, text = aiResponse)
            _uiState.update {
                it.copy(
                    messages = it.messages + aiMsg,
                    isTyping = false
                )
            }
        }
    }

    private suspend fun generateAiResponse(userPrompt: String): String {
        val prompt = userPrompt.lowercase()
        val today = LocalDate.now()
        val lunar = lunarConverter.solarToLunar(today.year, today.monthValue, today.dayOfMonth)
        val auspicious = auspiciousCalculator.calculate(lunar)

        val birthday = userPreferences.birthdayFlow.firstOrNull()
        val birthYear = if (birthday != null && birthday.third > 1900) birthday.third else 1995
        val zodiacInfo = EasternFengShuiHelper.getZodiacInfo(birthYear)

        return when {
            prompt.contains("tử vi") || prompt.contains("hôm nay") || prompt.contains("vận mệnh") || prompt.contains("tuổi") || prompt.contains("mệnh") -> {
                "🔮 **Tử vi & Phong thủy Phương Đông (${zodiacInfo.canChiYear} - ${zodiacInfo.animalEmoji})**:\n" +
                        "- **Bản mệnh**: Mệnh ${zodiacInfo.nguHanh.nameVi} (${zodiacInfo.napAm})\n" +
                        "- **Hôm nay**: Ngày ${lunar.canChiDay}, ${if (auspicious.isHoangDao) "Hoàng Đạo cát lành" else "Hắc Đạo"}, Trực **${auspicious.truc}**.\n" +
                        "- **Màu may mắn**: ${zodiacInfo.luckyColors.joinToString(", ")}\n" +
                        "- **Quy luật tương sinh**: ${zodiacInfo.suitableElements.joinToString("; ")}\n" +
                        "- **Lời khuyên**: Hãy giữ tâm thế tự tin, xuất hành hướng ${if (auspicious.isHoangDao) "Tây Nam (Hỷ Thần)" else "Đông Nam (Tài Thần)"} để thuận lợi công việc!"
            }
            prompt.contains("giờ đẹp") || prompt.contains("xuất hành") || prompt.contains("hoàng đạo") -> {
                val hours = if (auspicious.gioHoangDao.isNotEmpty()) auspicious.gioHoangDao.joinToString(", ") else "5:00-7:00 (Mão), 9:00-11:00 (Tỵ), 15:00-17:00 (Thân)"
                "⭐ **Giờ tốt Hoàng Đạo hôm nay** (${lunar.day}/${lunar.month} Âm lịch - Ngày ${lunar.canChiDay}):\n" +
                        "- Các khung giờ cát lành: $hours.\n" +
                        "- Hướng xuất hành đại cát: Hỷ Thần hướng Tây Nam, Tài Thần hướng Đông Nam."
            }
            prompt.contains("lịch") || prompt.contains("họp") || prompt.contains("công việc") || prompt.contains("task") -> {
                "🗓️ Tôi đã ghi nhận yêu cầu của bạn! Bạn có thể quản lý chi tiết trong tab **Task** hoặc theo dõi lịch trình trong tab **Hôm nay**."
            }
            prompt.contains("ngày tốt") || prompt.contains("cưới") || prompt.contains("mua xe") || prompt.contains("nhà") -> {
                "🎋 Ngày hôm nay (${lunar.day}/${lunar.month} Âm lịch) là ngày **${if (auspicious.isHoangDao) "Hoàng Đạo (Đại Cát)" else "Bình Hòa"}**, Trực **${auspicious.truc}**. Thích hợp cho việc khởi động dự án, ký kết hoặc hội họp."
            }
            else -> {
                "Cảm ơn bạn! Hôm nay là ngày ${today.dayOfMonth}/${today.monthValue} (Âm lịch ${lunar.day}/${lunar.month} - ${lunar.canChiDay}). Tuổi của bạn là **${zodiacInfo.canChiYear} ${zodiacInfo.animalEmoji}**, Mệnh **${zodiacInfo.nguHanh.nameVi}**. Nếu bạn cần xem ngày tốt, giờ hoàng đạo hoặc lên lịch trình, hãy nhắn cho tôi nhé!"
            }
        }
    }
}
