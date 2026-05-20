package com.example.uniplanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE  // изтриване на предмет → изтрива задачите му
        )
    ],
    indices = [Index("subjectId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val subjectId: Long,
    val deadline: Long,        // съхраняваме като timestamp (milliseconds)
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val imagePath: String? = null,   // път до снимката на записките
    val createdAt: Long = System.currentTimeMillis()
)

enum class Priority { LOW, MEDIUM, HIGH }

enum class TaskStatus { PENDING, IN_PROGRESS, DONE }