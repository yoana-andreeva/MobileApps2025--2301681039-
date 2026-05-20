package com.example.uniplanner.viewmodel

import com.example.uniplanner.data.local.dao.SubjectDao
import com.example.uniplanner.data.local.dao.TaskDao
import com.example.uniplanner.data.local.entity.Priority
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.data.repository.UniPlannerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RepositoryTest {

    private lateinit var subjectDao: SubjectDao
    private lateinit var taskDao: TaskDao
    private lateinit var repository: UniPlannerRepository

    private val fakeSubject = Subject(
        id = 1L,
        name = "Математика",
        color = 0xFF1565C0.toInt()
    )

    private val fakeTask = Task(
        id = 1L,
        title = "Домашно",
        subjectId = 1L,
        deadline = System.currentTimeMillis() + 86400000,
        priority = Priority.MEDIUM,
        status = TaskStatus.PENDING
    )

    @Before
    fun setup() {
        subjectDao = mock()
        taskDao = mock()
        repository = UniPlannerRepository(subjectDao, taskDao)

        whenever(subjectDao.getAllSubjects()).thenReturn(flowOf(listOf(fakeSubject)))
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(listOf(fakeTask)))
        whenever(taskDao.getPendingTasks()).thenReturn(flowOf(listOf(fakeTask)))
    }

    @Test
    fun `getAllSubjects returns subjects from dao`() = runTest {
        val result = mutableListOf<List<Subject>>()
        repository.getAllSubjects().collect { result.add(it) }
        assertEquals(listOf(fakeSubject), result.first())
    }

    @Test
    fun `getAllTasks returns tasks from dao`() = runTest {
        val result = mutableListOf<List<Task>>()
        repository.getAllTasks().collect { result.add(it) }
        assertEquals(listOf(fakeTask), result.first())
    }

    @Test
    fun `insertTask delegates to dao`() = runTest {
        whenever(taskDao.insertTask(fakeTask)).thenReturn(1L)
        repository.insertTask(fakeTask)
        verify(taskDao).insertTask(fakeTask)
    }

    @Test
    fun `deleteTask delegates to dao`() = runTest {
        repository.deleteTask(fakeTask)
        verify(taskDao).deleteTask(fakeTask)
    }

    @Test
    fun `insertSubject delegates to dao`() = runTest {
        whenever(subjectDao.insertSubject(fakeSubject)).thenReturn(1L)
        repository.insertSubject(fakeSubject)
        verify(subjectDao).insertSubject(fakeSubject)
    }

    @Test
    fun `deleteSubject delegates to dao`() = runTest {
        repository.deleteSubject(fakeSubject)
        verify(subjectDao).deleteSubject(fakeSubject)
    }
}