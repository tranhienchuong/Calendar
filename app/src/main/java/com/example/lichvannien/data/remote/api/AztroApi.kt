package com.example.lichvannien.data.remote.api

import com.example.lichvannien.data.remote.dto.HoroscopeResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AztroApi {
    @FormUrlEncoded
    @POST(".")
    suspend fun getHoroscope(
        @Field("sign") sign: String,
        @Field("day") day: String
    ): HoroscopeResponse
}
