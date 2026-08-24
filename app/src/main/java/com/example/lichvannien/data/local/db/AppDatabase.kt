package com.example.lichvannien.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lichvannien.data.local.entity.SpecialDayEntity
import com.example.lichvannien.data.local.entity.TaskEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

@Database(entities = [SpecialDayEntity::class, TaskEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun specialDayDao(): SpecialDayDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lich_van_nien_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Executors.newSingleThreadExecutor().execute {
                try {
                    // 1. Preload special days if empty
                    val cursor = db.query("SELECT COUNT(*) FROM special_days")
                    var count = 0
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0)
                    }
                    cursor.close()

                    if (count == 0) {
                        val jsonString = context.assets.open("special_days.json").bufferedReader().use { it.readText() }
                        val json = Json { ignoreUnknownKeys = true }
                        val dtos = json.decodeFromString<List<SpecialDayDto>>(jsonString)

                        db.beginTransaction()
                        try {
                            for (dto in dtos) {
                                val cv = ContentValues().apply {
                                    put("name", dto.name)
                                    if (dto.solarMonth != null) put("solarMonth", dto.solarMonth) else putNull("solarMonth")
                                    if (dto.solarDay != null) put("solarDay", dto.solarDay) else putNull("solarDay")
                                    if (dto.lunarMonth != null) put("lunarMonth", dto.lunarMonth) else putNull("lunarMonth")
                                    if (dto.lunarDay != null) put("lunarDay", dto.lunarDay) else putNull("lunarDay")
                                    put("isLunar", if (dto.isLunar) 1 else 0)
                                    put("leapMonth", if (dto.leapMonth) 1 else 0)
                                    put("icon", dto.icon)
                                }
                                db.insert("special_days", SQLiteDatabase.CONFLICT_REPLACE, cv)
                            }
                            db.setTransactionSuccessful()
                            Log.d("AppDatabase", "Preloaded ${dtos.size} special days successfully.")
                        } finally {
                            db.endTransaction()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error preloading initial data from assets", e)
                }
            }
        }
    }
}

@Serializable
data class SpecialDayDto(
    val name: String,
    val solarMonth: Int? = null,
    val solarDay: Int? = null,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
    val isLunar: Boolean,
    val leapMonth: Boolean = false,
    val icon: String? = null
)
