package com.example.uniplanner.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.uniplanner.data.local.entity.Task
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    // Колко рано да се изпрати нотификацията (по подразбиране 1 час)
    private val reminderOffsetMs = 60 * 60 * 1000L

    fun scheduleReminder(task: Task) {
        val delay = task.deadline - System.currentTimeMillis() - reminderOffsetMs
        if (delay <= 0) return // вече е минал срокът

        val data = Data.Builder()
            .putString(TaskReminderWorker.KEY_TASK_TITLE, task.title)
            .putLong(TaskReminderWorker.KEY_TASK_ID, task.id)
            .build()

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("task_${task.id}")
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelReminder(taskId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_$taskId")
    }
}