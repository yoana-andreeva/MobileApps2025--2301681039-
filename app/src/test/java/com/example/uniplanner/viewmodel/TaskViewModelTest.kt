package com.example.uniplanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.uniplanner.data.local.entity.Priority
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.data.repository.UniPlannerRepository
import com.example.uniplanner.worker.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.example.uniplanner.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UniPlannerRepository
    private lateinit var scheduler: NotificationScheduler
    private lateinit var viewModel: TaskViewModel

    private val fakeTask = Task(
        id = 1L,
        title = "Тест задача",
        subjectId = 1L,
        deadline = System.currentTimeMillis() + 86400000,
        priority = Priority.HIGH,
        status = TaskStatus.PENDING
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        scheduler = mock()

        whenever(repository.getAllTasks()).thenReturn(flowOf(listOf(fakeTask)))
        whenever(repository.getPendingTasks()).thenReturn(flowOf(listOf(fakeTask)))

        viewModel = TaskViewModel(repository, scheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertTask calls repository and schedules notification`() = runTest {
        whenever(repository.insertTask(fakeTask)).thenReturn(1L)

        viewModel.insertTask(fakeTask)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).insertTask(fakeTask)
        verify(scheduler).scheduleReminder(fakeTask.copy(id = 1L))
    }

    @Test
    fun `deleteTask calls repository and cancels notification`() = runTest {
        viewModel.deleteTask(fakeTask)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).deleteTask(fakeTask)
        verify(scheduler).cancelReminder(fakeTask.id)
    }

    @Test
    fun `updateTaskStatus to DONE cancels notification`() = runTest {
        viewModel.updateTaskStatus(fakeTask, TaskStatus.DONE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).updateTask(fakeTask.copy(status = TaskStatus.DONE))
        verify(scheduler).cancelReminder(fakeTask.id)
    }

    @Test
    fun `updateTaskStatus to PENDING does not cancel notification`() = runTest {
        viewModel.updateTaskStatus(fakeTask, TaskStatus.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).updateTask(fakeTask.copy(status = TaskStatus.PENDING))
    }
}