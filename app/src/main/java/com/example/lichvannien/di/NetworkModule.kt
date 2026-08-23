package com.example.lichvannien.di

import com.example.lichvannien.data.remote.api.AztroApi
import com.example.lichvannien.data.repository.HoroscopeRepositoryImpl
import com.example.lichvannien.domain.repository.HoroscopeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAztroApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): AztroApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://aztro.sameerkumar.website/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AztroApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HoroscopeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHoroscopeRepository(
        horoscopeRepositoryImpl: HoroscopeRepositoryImpl
    ): HoroscopeRepository
}
