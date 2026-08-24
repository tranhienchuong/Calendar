package com.example.lichvannien.domain.repository

import com.example.lichvannien.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>
    suspend fun getTasksForDateSync(date: String): List<TaskEntity>
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    suspend fun searchTasksSync(query: String): List<TaskEntity>
    suspend fun getTaskById(id: Long): TaskEntity?
    suspend fun getRecurringTasks(): List<TaskEntity>
    suspend fun addTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean)
    suspend fun deleteTask(id: Long)
}

