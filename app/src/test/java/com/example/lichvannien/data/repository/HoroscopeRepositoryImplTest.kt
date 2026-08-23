package com.example.lichvannien.data.repository

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.lichvannien.data.local.db.HoroscopeCacheDao
import com.example.lichvannien.data.local.entity.HoroscopeCacheEntity
import com.example.lichvannien.data.remote.api.AztroApi
import com.example.lichvannien.data.remote.dto.HoroscopeResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class HoroscopeRepositoryImplTest {

    private val aztroApi = mockk<AztroApi>()
    private val horoscopeCacheDao = mockk<HoroscopeCacheDao>(relaxed = true)
    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()

    private lateinit var repository: HoroscopeRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        every { context.assets } returns assetManager
        repository = HoroscopeRepositoryImpl(aztroApi, horoscopeCacheDao, context)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun testGetHoroscopeFromCacheWhenValid() = runTest {
        // Arrange
        val sign = "aries"
        val dayType = "today"
        val rawJson = """
            {
                "current_date": "Today",
                "description": "Energetic day",
                "compatibility": "Leo",
                "mood": "Confident",
                "color": "Red",
                "lucky_number": "9",
                "lucky_time": "9:00 AM"
            }
        """.trimIndent()
        val cache = HoroscopeCacheEntity(sign, dayType, rawJson, System.currentTimeMillis())

        every { horoscopeCacheDao.getCache(sign, dayType) } returns cache

        // Act
        val result = repository.getHoroscope(sign, dayType)

        // Assert
        assertThat(result.isSuccess).isTrue()
        val horoscope = result.getOrNull()
        assertThat(horoscope).isNotNull()
        assertThat(horoscope?.description).isEqualTo("Energetic day")
        assertThat(horoscope?.compatibility).isEqualTo("Leo")

        coVerify(exactly = 0) { aztroApi.getHoroscope(any(), any()) }
    }

    @Test
    fun testGetHoroscopeFromApiWhenNoCache() = runTest {
        // Arrange
        val sign = "aries"
        val dayType = "today"
        val apiResponse = HoroscopeResponse(
            current_date = "Today",
            description = "Energetic from API",
            compatibility = "Leo",
            mood = "Happy",
            color = "Blue",
            lucky_number = "7",
            lucky_time = "8:00 AM"
        )

        every { horoscopeCacheDao.getCache(sign, dayType) } returns null
        coEvery { aztroApi.getHoroscope(sign, dayType) } returns apiResponse

        // Act
        val result = repository.getHoroscope(sign, dayType)

        // Assert
        assertThat(result.isSuccess).isTrue()
        val horoscope = result.getOrNull()
        assertThat(horoscope?.description).isEqualTo("Energetic from API")
        assertThat(horoscope?.mood).isEqualTo("Happy")

        verify(exactly = 1) { horoscopeCacheDao.insert(any()) }
    }

    @Test
    fun testGetHoroscopeFromOldCacheWhenApiFails() = runTest {
        // Arrange
        val sign = "aries"
        val dayType = "today"
        val rawJson = """
            {
                "current_date": "Yesterday",
                "description": "Old energetic day",
                "compatibility": "Leo",
                "mood": "Confident",
                "color": "Red",
                "lucky_number": "9",
                "lucky_time": "9:00 AM"
            }
        """.trimIndent()
        // Cache hết hạn (25h trước)
        val oldCache = HoroscopeCacheEntity(
            sign = sign,
            dayType = dayType,
            jsonData = rawJson,
            timestamp = System.currentTimeMillis() - 25 * 60 * 60 * 1000L
        )

        every { horoscopeCacheDao.getCache(sign, dayType) } returns oldCache
        coEvery { aztroApi.getHoroscope(sign, dayType) } throws Exception("Network Error")

        // Act
        val result = repository.getHoroscope(sign, dayType)

        // Assert
        assertThat(result.isSuccess).isTrue()
        val horoscope = result.getOrNull()
        assertThat(horoscope?.description).isEqualTo("Old energetic day")
    }

    @Test
    fun testGetHoroscopeFromAssetsFallbackWhenCacheAndApiFail() = runTest {
        // Arrange
        val sign = "aries"
        val dayType = "today"
        val mockJson = """
        {
          "aries": {
            "today": {
              "current_date": "Today fallback",
              "description": "Fallback mock text",
              "compatibility": "Leo",
              "mood": "Confident",
              "color": "Red",
              "lucky_number": "9",
              "lucky_time": "9:00 AM"
            }
          }
        }
        """.trimIndent()

        every { horoscopeCacheDao.getCache(sign, dayType) } returns null
        coEvery { aztroApi.getHoroscope(sign, dayType) } throws Exception("Network Error")
        
        val inputStream = ByteArrayInputStream(mockJson.toByteArray())
        every { assetManager.open("horoscope_fallback.json") } returns inputStream

        // Act
        val result = repository.getHoroscope(sign, dayType)

        // Assert
        assertThat(result.isSuccess).isTrue()
        val horoscope = result.getOrNull()
        assertThat(horoscope?.description).isEqualTo("Fallback mock text")
        assertThat(horoscope?.date).isEqualTo("Today fallback")
    }
}
