package com.example.uniplanner.data.local.dao

import androidx.room.*
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY deadline ASC")
    fun getTasksBySubject(subjectId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status != :doneStatus ORDER BY deadline ASC")
    fun getPendingTasks(doneStatus: TaskStatus = TaskStatus.DONE): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE deadline BETWEEN :start AND :end ORDER BY deadline ASC")
    fun getTasksForDateRange(start: Long, end: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE status = :status AND deadline < :before")
    suspend fun deleteOldCompletedTasks(
        status: TaskStatus = TaskStatus.DONE,
        before: Long = System.currentTimeMillis()
    )
}