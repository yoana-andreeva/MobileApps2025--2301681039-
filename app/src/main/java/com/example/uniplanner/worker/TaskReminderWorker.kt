package com.example.uniplanner.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uniplanner.MainActivity
import com.example.uniplanner.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TASK_ID = "task_id"
    }

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)

        createNotificationChannel()
        showNotification(taskTitle, taskId)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напомняния за задачи",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Напомняния преди краен срок на задача"
            }
            val manager = applicationContext
                .getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(taskTitle: String, taskId: Long) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tasks)
            .setContentTitle("⏰ Наближава краен срок!")
            .setContentText("\"$taskTitle\" е с наближаващ срок")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(taskId.toInt(), notification)
    }
}