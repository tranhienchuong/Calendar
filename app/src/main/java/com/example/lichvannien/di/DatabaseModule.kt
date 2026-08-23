package com.example.lichvannien.di

import android.content.Context
import com.example.lichvannien.data.local.db.AppDatabase
import com.example.lichvannien.data.local.db.SpecialDayDao
import com.example.lichvannien.data.local.db.TaskDao
import com.example.lichvannien.data.repository.SpecialDayRepositoryImpl
import com.example.lichvannien.data.repository.TaskRepositoryImpl
import com.example.lichvannien.domain.repository.SpecialDayRepository
import com.example.lichvannien.domain.repository.TaskRepository
import com.example.lichvannien.domain.util.AuspiciousCalculator
import com.example.lichvannien.domain.util.LunarConverter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSpecialDayDao(database: AppDatabase): SpecialDayDao {
        return database.specialDayDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideLunarConverter(): LunarConverter = LunarConverter

    @Provides
    @Singleton
    fun provideAuspiciousCalculator(): AuspiciousCalculator = AuspiciousCalculator
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSpecialDayRepository(
        specialDayRepositoryImpl: SpecialDayRepositoryImpl
    ): SpecialDayRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}
