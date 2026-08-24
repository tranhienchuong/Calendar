package com.example.lichvannien.domain.repository

import com.example.lichvannien.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksForDate(date: String): Flow<List<Task>>
    suspend fun getTasksForDateSync(date: String): List<Task>
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun searchTasksSync(query: String): List<Task>
    suspend fun getTaskById(id: Long): Task?
    suspend fun getRecurringTasks(): List<Task>
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun updateTasks(tasks: List<Task>)
    suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean)
    suspend fun deleteTask(id: Long)
}

