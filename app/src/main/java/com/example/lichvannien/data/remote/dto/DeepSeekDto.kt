package com.example.lichvannien.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekChatRequest(
    val model: String = "deepseek-v4-flash",
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 4096,
    val stream: Boolean = false
)

@Serializable
data class DeepSeekMessage(
    val role: String,
    val content: String
)

@Serializable
data class DeepSeekChatResponse(
    val id: String? = null,
    val choices: List<DeepSeekChoice> = emptyList(),
    val error: DeepSeekError? = null
)

@Serializable
data class DeepSeekChoice(
    val index: Int = 0,
    val message: DeepSeekMessage,
    val finish_reason: String? = null
)

@Serializable
data class DeepSeekError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)

@Serializable
data class DeepSeekStreamChunk(
    val id: String? = null,
    val choices: List<DeepSeekStreamChoice> = emptyList()
)

@Serializable
data class DeepSeekStreamChoice(
    val index: Int = 0,
    val delta: DeepSeekStreamDelta? = null,
    val finish_reason: String? = null
)

@Serializable
data class DeepSeekStreamDelta(
    val role: String? = null,
    val content: String? = null
)
