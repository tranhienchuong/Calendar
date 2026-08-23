package com.example.lichvannien.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lichvannien.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, date ASC, startTime ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, startTime ASC")
    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, startTime ASC")
    fun getTasksForDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY date DESC, startTime ASC")
    fun searchTasksFlow(query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY date DESC, startTime ASC")
    fun searchTasks(query: String): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Update
    fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    fun setTaskCompleted(id: Long, isCompleted: Boolean)

    @Delete
    fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    fun deleteTaskById(id: Long)
}
