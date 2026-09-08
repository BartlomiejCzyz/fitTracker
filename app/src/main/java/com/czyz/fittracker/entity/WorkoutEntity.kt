package com.czyz.fittracker.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["workoutPlanId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val workoutId: Int = 0,
    val planId: Int,
    val title: String,
    val dateTimestamp: Long
)
