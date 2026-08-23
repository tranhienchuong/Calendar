package com.example.lichvannien.data.remote.api

import com.example.lichvannien.data.remote.dto.DeepSeekChatRequest
import com.example.lichvannien.data.remote.dto.DeepSeekChatResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DeepSeekApi {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: DeepSeekChatRequest
    ): Response<DeepSeekChatResponse>

    @Streaming
    @POST("chat/completions")
    suspend fun streamChatCompletion(
        @Body request: DeepSeekChatRequest
    ): Response<ResponseBody>
}
