package com.czyz.fittracker.entity

import androidx.room.Embedded
import androidx.room.Relation

// 1. Połączenie instancji w treningu z jej nazwą ze słownika i jej seriami
data class WorkoutExerciseDetails(
    @Embedded
    val workoutExercise: WorkoutExerciseEntity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exerciseCatalog: ExerciseEntity,

    @Relation(
        parentColumn = "workoutExerciseId",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<ExerciseSetEntity>
)

// 2. Pełny Trening wraz ze wszystkimi ćwiczeniami i ich seriami
data class WorkoutWithDetails(
    @Embedded
    val workout: WorkoutEntity,

    @Relation(
        entity = WorkoutExerciseEntity::class,
        parentColumn = "workoutId",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseDetails>
)
