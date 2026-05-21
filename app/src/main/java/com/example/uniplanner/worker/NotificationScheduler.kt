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
    // Взимаме SharedPreferences на приложението
    private val sharedPreferences = context.getSharedPreferences("uniplanner_prefs", Context.MODE_PRIVATE)

    // Метод, който изчислява динамично offset-а в милисекунди
    private fun getReminderOffsetMs(): Long {
        val hours = sharedPreferences.getInt("reminder_hours", 1) // 1 час по подразбиране
        return hours * 60 * 60 * 1000L
    }

    fun scheduleReminder(task: Task) {
        // Използваме динамичния offset тук
        val delay = task.deadline - System.currentTimeMillis() - getReminderOffsetMs()
        if (delay <= 0) return // Вече е минал срокът или е твърде късно за напомняне

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