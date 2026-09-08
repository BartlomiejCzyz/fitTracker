package com.czyz.fittracker.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val workoutPlanId: Int = 0,
    val title: String,
    val description: String
)

