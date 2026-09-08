package com.czyz.fittracker.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val exerciseId: Int = 0,
    val name: String,
    val targetRepetitions: Int,
    val targetWeight: Double,
    val sortOrder: Int = 0,
    val planId: Int = 0
)
