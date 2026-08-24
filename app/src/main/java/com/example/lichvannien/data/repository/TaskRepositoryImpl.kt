package com.example.lichvannien.data.repository

import com.example.lichvannien.data.local.db.TaskDao
import com.example.lichvannien.data.mapper.toDomain
import com.example.lichvannien.data.mapper.toEntity
import com.example.lichvannien.domain.model.Task
import com.example.lichvannien.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksFlow().map { it.toDomain() }.flowOn(Dispatchers.IO)
    }

    override fun getTasksForDate(date: String): Flow<List<Task>> {
        return taskDao.getTasksForDateFlow(date).map { it.toDomain() }.flowOn(Dispatchers.IO)
    }

    override suspend fun getTasksForDateSync(date: String): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getTasksForDate(date).toDomain()
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasksFlow(query).map { it.toDomain() }.flowOn(Dispatchers.IO)
    }

    override suspend fun searchTasksSync(query: String): List<Task> = withContext(Dispatchers.IO) {
        taskDao.searchTasks(query).toDomain()
    }

    override suspend fun getTaskById(id: Long): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun getRecurringTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getRecurringTasks().toDomain()
    }

    override suspend fun addTask(task: Task): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun updateTasks(tasks: List<Task>) = withContext(Dispatchers.IO) {
        taskDao.updateTasks(tasks.toEntity())
    }

    override suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setTaskCompleted(id, isCompleted)
    }

    override suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }
}
