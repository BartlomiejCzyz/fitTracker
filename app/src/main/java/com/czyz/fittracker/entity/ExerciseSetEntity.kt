package com.czyz.fittracker.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["workoutExerciseId"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE // Usunięcie ćwiczenia z treningu kasuje jego serie
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Int = 0,
    val workoutExerciseId: Int,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int
)
