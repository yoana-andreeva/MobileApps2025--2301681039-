package com.example.uniplanner.data.repository

import com.example.uniplanner.data.local.dao.SubjectDao
import com.example.uniplanner.data.local.dao.TaskDao
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniPlannerRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao
) {
    // ─── Subjects ───────────────────────────────
    fun getAllSubjects(): Flow<List<Subject>> =
        subjectDao.getAllSubjects()

    suspend fun getSubjectById(id: Long): Subject? =
        subjectDao.getSubjectById(id)

    suspend fun insertSubject(subject: Subject): Long =
        subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: Subject) =
        subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: Subject) =
        subjectDao.deleteSubject(subject)

    // ─── Tasks ──────────────────────────────────
    fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks()

    fun getTasksBySubject(subjectId: Long): Flow<List<Task>> =
        taskDao.getTasksBySubject(subjectId)

    fun getPendingTasks(): Flow<List<Task>> =
        taskDao.getPendingTasks()

    fun getTasksForDateRange(start: Long, end: Long): Flow<List<Task>> =
        taskDao.getTasksForDateRange(start, end)

    suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(task)

    suspend fun updateTask(task: Task) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task)

    suspend fun deleteOldCompletedTasks() =
        taskDao.deleteOldCompletedTasks()
}