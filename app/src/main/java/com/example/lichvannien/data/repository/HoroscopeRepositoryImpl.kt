package com.example.lichvannien.data.repository

import android.content.Context
import android.util.Log
import com.example.lichvannien.data.local.db.HoroscopeCacheDao
import com.example.lichvannien.data.local.entity.HoroscopeCacheEntity
import com.example.lichvannien.data.remote.api.AztroApi
import com.example.lichvannien.data.remote.dto.HoroscopeResponse
import com.example.lichvannien.domain.model.Horoscope
import com.example.lichvannien.domain.repository.HoroscopeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HoroscopeRepositoryImpl @Inject constructor(
    private val aztroApi: AztroApi,
    private val horoscopeCacheDao: HoroscopeCacheDao,
    @ApplicationContext private val context: Context
) : HoroscopeRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val cacheExpiryMillis = 24 * 60 * 60 * 1000L // 24 giờ

    override suspend fun getHoroscope(sign: String, dayType: String): Result<Horoscope> = withContext(Dispatchers.IO) {
        val lowercaseSign = sign.lowercase()
        
        try {
            // 1. Kiểm tra cache trong DB
            val cache = horoscopeCacheDao.getCache(lowercaseSign, dayType)
            if (cache != null && (System.currentTimeMillis() - cache.timestamp) < cacheExpiryMillis) {
                val cachedResponse = json.decodeFromString<HoroscopeResponse>(cache.jsonData)
                return@withContext Result.success(mapToDomain(lowercaseSign, cachedResponse))
            }

            // 2. Không có cache hoặc cache hết hạn -> gọi API
            try {
                val apiResponse = aztroApi.getHoroscope(lowercaseSign, dayType)
                
                // Lưu vào cache DB
                val jsonString = json.encodeToString(apiResponse)
                val newCache = HoroscopeCacheEntity(
                    sign = lowercaseSign,
                    dayType = dayType,
                    jsonData = jsonString,
                    timestamp = System.currentTimeMillis()
                )
                horoscopeCacheDao.insert(newCache)
                
                return@withContext Result.success(mapToDomain(lowercaseSign, apiResponse))
            } catch (apiException: Exception) {
                Log.e("HoroscopeRepository", "API error for $lowercaseSign on $dayType, falling back to cache", apiException)
                
                // 3. API lỗi -> Thử lấy cache cũ (dù đã quá 24 giờ)
                if (cache != null) {
                    val cachedResponse = json.decodeFromString<HoroscopeResponse>(cache.jsonData)
                    return@withContext Result.success(mapToDomain(lowercaseSign, cachedResponse))
                }
                
                // 4. Không có cache DB -> Đọc từ assets file offline
                return@withContext readFallbackFromAssets(lowercaseSign, dayType)
            }
        } catch (e: Exception) {
            Log.e("HoroscopeRepository", "Critical error fetching horoscope", e)
            return@withContext Result.failure(e)
        }
    }

    private fun readFallbackFromAssets(sign: String, dayType: String): Result<Horoscope> {
        return try {
            val jsonString = context.assets.open("horoscope_fallback.json")
                .bufferedReader()
                .use { it.readText() }
                
            val fallbackMap = json.decodeFromString<Map<String, Map<String, HoroscopeResponse>>>(jsonString)
            val response = fallbackMap[sign]?.get(dayType)
            
            if (response != null) {
                Result.success(mapToDomain(sign, response))
            } else {
                Result.failure(NoSuchElementException("No fallback found for $sign and $dayType"))
            }
        } catch (e: Exception) {
            Log.e("HoroscopeRepository", "Failed to load fallback horoscope from assets", e)
            Result.failure(e)
        }
    }

    private fun mapToDomain(sign: String, response: HoroscopeResponse): Horoscope {
        return Horoscope(
            sign = sign,
            date = response.current_date,
            description = response.description,
            compatibility = response.compatibility,
            mood = response.mood,
            color = response.color,
            luckyNumber = response.lucky_number,
            luckyTime = response.lucky_time
        )
    }
}
