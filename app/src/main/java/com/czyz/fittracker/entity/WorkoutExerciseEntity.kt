package com.czyz.fittracker.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("workoutId"),
        Index("exerciseId")
    ]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val workoutExerciseId: Int = 0,
    val workoutId: Int,
    val exerciseId: Int
)
