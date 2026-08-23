package com.example.lichvannien.data.repository

import com.example.lichvannien.data.local.db.TaskDao
import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasksFlow().flowOn(Dispatchers.IO)
    }

    override fun getTasksForDate(date: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDateFlow(date).flowOn(Dispatchers.IO)
    }

    override suspend fun getTasksForDateSync(date: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getTasksForDate(date)
    }

    override fun searchTasks(query: String): Flow<List<TaskEntity>> {
        return taskDao.searchTasksFlow(query).flowOn(Dispatchers.IO)
    }

    override suspend fun searchTasksSync(query: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.searchTasks(query)
    }

    override suspend fun addTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    override suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setTaskCompleted(id, isCompleted)
    }

    override suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }
}
