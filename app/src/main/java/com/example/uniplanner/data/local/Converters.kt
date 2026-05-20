package com.example.uniplanner.data.local

import androidx.room.TypeConverter
import com.example.uniplanner.data.local.entity.Priority
import com.example.uniplanner.data.local.entity.TaskStatus

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}