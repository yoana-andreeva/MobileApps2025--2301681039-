package com.example.uniplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,        // съхраняваме като Int (Color.parseColor)
    val teacher: String = "",
    val room: String = ""
)