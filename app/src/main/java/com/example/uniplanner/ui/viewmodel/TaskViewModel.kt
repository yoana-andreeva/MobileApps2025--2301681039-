package com.example.uniplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniplanner.data.local.entity.Task
import com.example.uniplanner.data.local.entity.TaskStatus
import com.example.uniplanner.data.repository.UniPlannerRepository
import com.example.uniplanner.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: UniPlannerRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<Task>> = repository.getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)

    fun insertTask(task: Task) = viewModelScope.launch {
        val id = repository.insertTask(task)
        notificationScheduler.scheduleReminder(task.copy(id = id))
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
        notificationScheduler.cancelReminder(task.id)
        notificationScheduler.scheduleReminder(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        notificationScheduler.cancelReminder(task.id)
    }

    fun updateTaskStatus(task: Task, status: TaskStatus) = viewModelScope.launch {
        repository.updateTask(task.copy(status = status))
        if (status == TaskStatus.DONE) {
            notificationScheduler.cancelReminder(task.id)
        }
    }

    fun getTasksForDateRange(start: Long, end: Long) =
        repository.getTasksForDateRange(start, end)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteOldCompletedTasks() = viewModelScope.launch {
        repository.deleteOldCompletedTasks()
    }
}