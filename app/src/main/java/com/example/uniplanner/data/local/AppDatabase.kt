package com.example.uniplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.uniplanner.data.local.dao.SubjectDao
import com.example.uniplanner.data.local.dao.TaskDao
import com.example.uniplanner.data.local.entity.Subject
import com.example.uniplanner.data.local.entity.Task

@Database(
    entities = [Subject::class, Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
}