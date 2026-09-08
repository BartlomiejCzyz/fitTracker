package com.czyz.fittracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.czyz.fittracker.dao.ExerciseDao
import com.czyz.fittracker.dao.WorkoutDao
import com.czyz.fittracker.entity.*

@Database(
    entities = [
        WorkoutPlanEntity::class,
        WorkoutEntity::class,
        ExerciseEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseSetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
}